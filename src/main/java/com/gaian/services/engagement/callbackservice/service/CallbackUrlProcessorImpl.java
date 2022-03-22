package com.gaian.services.engagement.callbackservice.service;

import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_CALLBACK_DELIVERY;
import static com.gaian.services.engagement.callbackservice.utils.CallbackUtils.collectAsyncContent;
import static com.gaian.services.engagement.model.request.DataSourceEnum.DESTINATION;
import static com.gaian.services.engagement.model.request.DataSourceEnum.SOURCE;
import static com.gaian.services.engagement.model.request.DataSourceEnum.TRIGGER;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.EMPTY_MAP;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.gaian.services.engagement.callbackservice.error.exception.CallbackDeliveryException;
import com.gaian.services.engagement.content.processor.ContentProcessor;
import com.gaian.services.engagement.dao.EngagementInstanceDao;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.db.AsyncContentRequest;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.db.EngagementInstance;
import com.gaian.services.engagement.model.db.EngagementTransaction;
import com.gaian.services.engagement.model.request.DataSourceEnum;
import com.gaian.services.engagement.model.request.channel.CallbackUrl;
import com.gaian.services.engagement.model.request.content.ContentModel;
import com.gaian.services.engagement.model.request.content.ContentType;
import com.gaian.services.engagement.model.request.content.DataSourceMapping;
import com.gaian.services.engagement.repo.AsyncContentRequestRepo;
import com.gaian.services.engagement.repo.EngagementInstanceRepo;
import com.gaian.services.engagement.repo.EngagementTransactionRepo;
import com.gaian.services.engagement.repo.EngagementsRepo;
import com.gaian.services.engagement.service.EngagementHelper;
import com.gaian.services.exception.ApplicationException;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CallbackUrlProcessorImpl implements CallbackUrlProcessor {

    @Autowired
    private EngagementHelper helper;

    @Autowired
    private Period defaultExpiryPeriod;

    @Autowired
    private PackageManager packageManager;

    @Autowired
    private EngagementsRepo engagementsRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EngagementInstanceRepo engagementInstanceRepo;

    @Autowired
    private AsyncContentRequestRepo asyncContentRequestRepo;

    @Autowired
    private EngagementInstanceDao engagementInstanceDao;

    @Autowired
    private EngagementTransactionRepo engagementTransactionRepo;

    @Autowired
    @Qualifier("content_processors")
    private Map<ContentType, ContentProcessor> contentProcessors;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${packageUpload.url}")
    private String uploadUrl;

    @Value("${callback.priority:HIGH}")
    private String callbackPriority;

    @Value("${package.extension:zip}")
    private String packageFileExtension;

    /**
     * Asynchronously starts processing a callback request
     *
     * @param inputRequest callback request
     * @return Completable future with ID of the engagement instance
     */


    @Async
    @Override
    public Future<String> initiateEngagementProcessing(DeliveryRequest inputRequest) {

        return new AsyncResult<>(processEngagement(inputRequest));
    }

    /**
     * Synchronously processes callback request
     *
     * @param inputRequest callback request
     * @return Transaction ID
     */
    @Override
    public String processEngagement(DeliveryRequest inputRequest) {

        log.info("Processing callback request {}", inputRequest);

        String transactionId = inputRequest.getTransactionId();
        EngagementDBModel engagement = inputRequest.getEngagement();
        String engagementId = engagement.getId();

        EngagementInstance instance = null;
        EngagementTransaction transaction = null;

        Map<DataSourceEnum, Object> data = ofNullable(inputRequest.getData()).orElse(EMPTY_MAP);

        try {
            transaction = engagementTransactionRepo.findById(transactionId)
                    .orElse(new EngagementTransaction(transactionId, engagementId, null));

            instance = new EngagementInstance(engagement.getId(), transactionId, data.get(TRIGGER), data.get(SOURCE),
                    data.get(DESTINATION), null);

        } catch (Exception exception) {
            log.error("Failed to start processing callback request {} ", inputRequest, exception);
            throw exception;
        }

        try {
            // Logging the initial status
            engagementInstanceRepo.save(instance);

            if (!initiateAsyncContentGeneration(engagement, data, instance)) {
                resumeEngagement(engagement, instance);
            }

        } catch (ApplicationException applicationException) {
            String errorMessage = format(ERROR_CALLBACK_DELIVERY, engagementId, transactionId);
            log.error(errorMessage, applicationException);
            throw transaction.addError(instance.addError(applicationException));

        } catch (Exception exception) {
            String errorMessage = format(ERROR_CALLBACK_DELIVERY, engagementId, transactionId);
            log.error(errorMessage, exception);
            throw transaction.addError(instance.addError(new CallbackDeliveryException(errorMessage, exception)));

        } finally {
            // logging the job status
            engagementInstanceRepo.save(instance.finish());
            engagementTransactionRepo.save(transaction.finish());
        }

        return instance.getId();
    }

    /**
     * Verifies if all the asynchronous contents (if any) are ready
     *
     * @param instanceId ID of the engagement instance
     * @return flag indication all contents are ready for engagement to be processed
     */
    @Override
    public boolean allAsyncContentsReceived(String instanceId) {

        return engagementInstanceRepo.findById(instanceId).map(EngagementInstance::getContentYetToBeReadyCount)
                .map(count -> count <= 0).orElse(false);
    }

    /**
     * Continues with the processing of engagement
     *
     * @param instanceId ID of engagement instance
     */
    @Override
    public void resumeEngagement(String instanceId) {

        log.info("Resuming engagement instance {}", instanceId);
        engagementInstanceRepo.findById(instanceId).ifPresent(instance -> engagementsRepo
                .findById(instance.getEngagementId()).ifPresent(engagement -> resumeEngagement(engagement, instance)));
    }

    /**
     * Continues with the processing of engagement
     *
     * @param engagement engagement request
     * @param instance   engagement instance
     */
    @Override
    public void resumeEngagement(EngagementDBModel engagengagementement, EngagementInstance instance) {

        CallbackUrl callbackUrl = CallbackUrl.class.cast(engagengagementement.getChannel());

        if (instance != null) {
            //String url = fromHttpUrl(callbackUrl.getUrl()).toUriString();
            String url = callbackUrl.getUrl();

//----------------- DataSource Mapping Starts-------------------------
            Map<String, Object> tempdataSources = new HashMap<String, Object>();
            List<DataSourceMapping> dataSources1 = callbackUrl.getDataSources();
            for (DataSourceMapping tempKey : dataSources1) {
                String key = tempKey.getKey();
                if (key == "source") {
                    tempdataSources.put(key, instance.getSourceData());
                } else if (key == "destination") {
                    tempdataSources.put(key, instance.getDestData());
                } else if (key == "trigger") {
                    tempdataSources.put(key, instance.getEvent());
                }
            }
//----------------	 DataSource Mapping Ends ----------------------------------------		

// --------------------Pathparam starts----------------------------------
            String startValue = "jsonPath:";
            URI uri = null;

            Map<String, String> pathparams = callbackUrl.getPathParams();

            Map<String, String> pathParamsMap = new HashMap<>();


            for (Map.Entry pathParamsEntry : pathparams.entrySet()) {

                Object tempValue = pathParamsEntry.getValue();
                if (tempValue instanceof String) {
                    String matchingValue = tempValue.toString();

                    if (matchingValue.startsWith(startValue)) {
                        String tempKey = pathParamsEntry.getKey().toString();

                        String tempParameterValue = matchingValue.split(":")[1];
                        String parameterValue = tempParameterValue.replace(".", ":").split(":")[0];
                        // saves val
                        Object temp = tempdataSources.get(parameterValue);


                        String tempDataSource[] = tempParameterValue.replace(".", ":").split(":");
                        String jsonPathExpression = "";
                        jsonPathExpression = jsonPathExpression + tempDataSource[1];
                        for (int i = 2; i < tempDataSource.length; i++) {
                            jsonPathExpression = jsonPathExpression + "." + tempDataSource[i];
                        }

                        Object parsed = JsonPath.parse(temp.toString()).read(jsonPathExpression, Object.class);

                        //Object resolvedValue = JsonPath.parse(resolvedJson).read(jsonPathExpression, Object.class);
                        String resValue = parsed.toString();
                        pathParamsMap.put(tempKey, resValue);


                    } else {
                        String tempKey = pathParamsEntry.getKey().toString();

                        pathParamsMap.put(tempKey, matchingValue);


                    }
                }
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

            Map<String, List<String>> queryParams = callbackUrl.getQueryParams();

            HashMap<String, List<String>> queryParamsResolved = new HashMap<String, List<String>>();
            for (Map.Entry queryParamsEntry : queryParams.entrySet()) {
                String key = (String) queryParamsEntry.getKey();
                List<String> templi = (List<String>) queryParamsEntry.getValue();
                for (int i = 0; i < templi.size(); i++) {
                    String tempValue = templi.get(i);

                    if (tempValue.startsWith(startValue)) {
                        String tempqueryParameterValue = tempValue.split(":")[1];
                        String queryparameterValue = tempqueryParameterValue.replace(".", ":").split(":")[0];
                        Object temp = tempdataSources.get(queryparameterValue);
                        String json = temp.toString();
                        ObjectMapper mapper = new ObjectMapper();
                        String resolvedJson = "";
                        try {
                            resolvedJson = mapper.writeValueAsString(temp);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        String tempDataSource[] = tempqueryParameterValue.replace(".", ":").split(":");
                        String jsonPathExpression = "";
                        jsonPathExpression = jsonPathExpression + tempDataSource[1];
                        for (int j = 2; j < tempDataSource.length; j++) {
                            jsonPathExpression = jsonPathExpression + "." + tempDataSource[j];
                        }
                        String parsed = JsonPath.parse(json).read(jsonPathExpression, String.class);
                        String resValue = parsed.toString();
                        templi.set(i, resValue);
                        queryParamsResolved.put(key, templi);

                    } else {
                        queryParamsResolved.put(key, templi);

                    }
                }

            }
            System.out.println(queryParamsResolved);
            //UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            for (Map.Entry queryParamsEntry : queryParamsResolved.entrySet()) {
                String key = (String) queryParamsEntry.getKey();
                List<String> value = (List<String>) queryParamsEntry.getValue();
                builder.queryParam(key, value);

            }
            System.out.println(builder.buildAndExpand(pathParamsMap).toUri());
            URI uri1 = builder.buildAndExpand(pathParamsMap).toUri();

            Map<String, Object> body = callbackUrl.getBody();
            Map<String, Object> resolvedBody = new HashMap<String, Object>();

            for (Map.Entry<String, Object> bodyEntry : body.entrySet()) {

                Object keyValue = bodyEntry.getValue();
                if (keyValue instanceof List<?>) {
                    List<Object> tempList = (List<Object>) keyValue;
                    for (int i = 0; i < tempList.size(); i++) {
                        Object tempElement = tempList.get(i);
                        if (tempElement instanceof String) {
                            String tempString = tempElement.toString();
                            if (tempString.startsWith(startValue)) {
                                String tempBodyParameterValue = tempString.split(":")[1];
                                String bodyValue = tempBodyParameterValue.replace(".", ":").split(":")[0];
                                Object temp = tempdataSources.get(bodyValue);
                                String tempDataSource[] = tempBodyParameterValue.replace(".", ":").split(":");
                                String jsonPathExpression = "";
                                jsonPathExpression = jsonPathExpression + tempDataSource[1];
                                for (int j = 2; j < tempDataSource.length; j++) {
                                    jsonPathExpression = jsonPathExpression + "." + tempDataSource[j];
                                }
                                Object parsed = JsonPath.parse(temp.toString()).read(jsonPathExpression, Object.class);
                                String resValue = parsed.toString();
                                tempList.set(i, resValue);
                            }
                        }

                    }

                } else if (keyValue instanceof String) {
                    String tempValue = keyValue.toString();

                    if (tempValue.startsWith(startValue)) {

                        String tempBodyStringValue = tempValue.split(":")[1];
                        String tempArray[] = tempBodyStringValue.replace(".", ":").split(":");
                        if (tempArray.length == 1) {
                            Object temp = tempdataSources.get(tempBodyStringValue);
                            String value = temp.toString();
                            bodyEntry.setValue(value);

                        } else {
                            String bodyValue = tempBodyStringValue.replace(".", ":").split(":")[0];
                            Object temp = tempdataSources.get(bodyValue);
                            String tempDataSource[] = tempBodyStringValue.replace(".", ":").split(":");
                            String jsonPathExpression = "";
                            jsonPathExpression = jsonPathExpression + tempDataSource[1];
                            for (int j = 2; j < tempDataSource.length; j++) {
                                jsonPathExpression = jsonPathExpression + "." + tempDataSource[j];
                            }
                            Object parsed = JsonPath.parse(temp.toString()).read(jsonPathExpression, Object.class);
                            String resValue = parsed.toString();
                            bodyEntry.setValue(resValue);

                        }

                    }

                } else if (keyValue instanceof Map) {
                    Map<String, Map<String, Object>> tempMap = (Map<String, Map<String, Object>>) keyValue;
                    for (Map.Entry<String, Map<String, Object>> elementEntry : tempMap.entrySet()) {

                        Map<String, Object> fieldsMap = elementEntry.getValue();

                        for (Map.Entry<String, Object> fieldEntry : fieldsMap.entrySet()) {
                            //System.out.println(fieldEntry.getValue());
                            String key = fieldEntry.getKey();
                            Object value = fieldEntry.getValue();
                            if (value instanceof String) {
                                String stringValue = value.toString();
                                if (stringValue.startsWith(startValue)) {

                                    String tempBodyParameterValue = stringValue.split(":")[1];
                                    String bodyValue = tempBodyParameterValue.replace(".", ":").split(":")[0];
                                    Object temp = tempdataSources.get(bodyValue);
                                    String tempDataSource[] = tempBodyParameterValue.replace(".", ":").split(":");
                                    String jsonPathExpression = "";
                                    jsonPathExpression = jsonPathExpression + tempDataSource[1];
                                    for (int j = 2; j < tempDataSource.length; j++) {
                                        jsonPathExpression = jsonPathExpression + "." + tempDataSource[j];
                                    }
                                    Object parsed = JsonPath.parse(temp.toString()).read(jsonPathExpression, Object.class);
                                    String resValue = parsed.toString();
                                    fieldEntry.setValue(parsed);
                                }

                            }
                        }

                    }
                }

            }

            restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(uri1,
                    HttpMethod.valueOf(callbackUrl.getHttpMethod().toString()), new HttpEntity(callbackUrl.getBody()),
                    String.class);

        }

    }

    /**
     * Triggers the generation of all the asynchronous contents involved (if any)
     *
     * @param engagement engagement request
     * @param data       all dynamic data involved
     * @param instance   engagement instance
     * @return flag indicating if there are any asynchronous content involved
     */
    @Override
    public boolean initiateAsyncContentGeneration(EngagementDBModel engagement, Map<DataSourceEnum, Object> data,
                                                  EngagementInstance instance) {

        List<ContentModel> asyncContentModels = collectAsyncContent(engagement);

        if (isNotEmpty(asyncContentModels)) {

            log.info("Triggering generation of async contents for callback {}", engagement.getId());

            Map<String, AsyncContentRequest> asyncContentsRequests = asyncContentModels.parallelStream()
                    .map(asyncContent -> {

                        // Logging all the async contents triggered
                        AsyncContentRequest asyncContentRequest = new AsyncContentRequest();
                        asyncContentRequest.setInstanceId(instance.getId());
                        asyncContentRequest.setEngagementId(engagement.getId());
                        asyncContentRequest.setTransactionId(instance.getTransactionId());
                        asyncContentRequest.setContent(asyncContent);
                        asyncContentRequestRepo.save(asyncContentRequest);

                        // Triggering async content
                        Object triggerDetails = contentProcessors.get(asyncContent.getContentType())
                                .triggerContentGeneration(engagement.getTenantId(), asyncContent, data.get(TRIGGER),
                                        data.get(SOURCE), data.get(DESTINATION), engagement,
                                        asyncContentRequest.getId())
                                .getResponse();

                        // Logging trigger responses
                        asyncContentRequestRepo.save(asyncContentRequest.setAsyncRequest(triggerDetails));

                        return new SimpleEntry<>(asyncContent.getIndex(), asyncContentRequest);

                    }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

            // Binding the content details with the engagement instance
            engagementInstanceRepo.save(instance.setAsyncContents(asyncContentsRequests));
            return true;
        }

        return false;
    }

    @Override
    public FileSystemResource downloadPackage(String tenantId, String transactionId, String engagementId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Processes the asynchronous content response
     *
     * @param contentResponse content response received
     */

}

/**
 * Downloads an callback package from a completed transaction of an callback
 * request
 *
 * @param tenantId      ID of tenant
 * @param transactionId ID of transaction
 * @param engagementId  ID of engagement
 * @return callback package
 */
