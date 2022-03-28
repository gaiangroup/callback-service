package com.gaian.services.engagement.apiservice.service;

import static com.gaian.services.engagement.api.error.ApiErrorTemplates.ERROR_API_DELIVERY;
import static com.gaian.services.engagement.apiservice.utils.ApiUtils.collectAsyncContent;
import static com.gaian.services.engagement.model.request.DataSourceEnum.DESTINATION;
import static com.gaian.services.engagement.model.request.DataSourceEnum.SOURCE;
import static com.gaian.services.engagement.model.request.DataSourceEnum.TRIGGER;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.EMPTY_MAP;

import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.gaian.services.engagement.model.request.channel.ApiUrl;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaian.services.engagement.apiservice.error.exception.ApiDeliveryException;
import com.gaian.services.engagement.content.processor.ContentProcessor;
import com.gaian.services.engagement.dao.EngagementInstanceDao;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.db.AsyncContentRequest;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.db.EngagementInstance;
import com.gaian.services.engagement.model.db.EngagementTransaction;
import com.gaian.services.engagement.model.request.DataSourceEnum;
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
public class ApiProcessorImpl implements ApiProcessor {

    @Autowired
    private EngagementHelper helper;

    @Autowired
    private Period defaultExpiryPeriod;

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

    @Value("${api.priority:HIGH}")
    private String apiPriority;

    @Value("${package.extension:zip}")
    private String packageFileExtension;

    /**
     * Asynchronously starts processing a api request
     *
     * @param inputRequest api request
     * @return Completable future with ID of the engagement instance
     */


    @Async
    @Override
    public Future<String> initiateEngagementProcessing(DeliveryRequest inputRequest) {

        return new AsyncResult<>(processEngagement(inputRequest));
    }

    /**
     * Synchronously processes api request
     *
     * @param inputRequest api request
     * @return Transaction ID
     */
    @Override
    public String processEngagement(DeliveryRequest inputRequest) {

        log.info("Processing api request {}", inputRequest);

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
            log.error("Failed to start processing api request {} ", inputRequest, exception);
            throw exception;
        }

        try {
            // Logging the initial status
            engagementInstanceRepo.save(instance);

            if (!initiateAsyncContentGeneration(engagement, data, instance)) {
                resumeEngagement(engagement, instance);
            }

        } catch (ApplicationException applicationException) {
            String errorMessage = format(ERROR_API_DELIVERY, engagementId, transactionId);
            log.error(errorMessage, applicationException);
            throw transaction.addError(instance.addError(applicationException));

        } catch (Exception exception) {
            String errorMessage = format(ERROR_API_DELIVERY, engagementId, transactionId);
            log.error(errorMessage, exception);
            throw transaction.addError(instance.addError(new ApiDeliveryException(errorMessage, exception)));

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
    Map<String, Object> dataSourcesMap = new HashMap<String, Object>();

    @Override
    public void resumeEngagement(EngagementDBModel engagement, EngagementInstance instance) {

        ApiUrl apiUrl = ApiUrl.class.cast(engagement.getChannel());
        if (instance != null) {
            String url = apiUrl.getUrl();
            List<DataSourceMapping> dataSources = apiUrl.getDataSources();

            dataSources.forEach(dataSource -> {
                String key = dataSource.getKey();
                if (key == "source") {
                    dataSourcesMap.put(key, instance.getSourceData());
                } else if (key == "destination") {
                    dataSourcesMap.put(key, instance.getDestData());
                } else if (key == "trigger") {
                    dataSourcesMap.put(key, instance.getEvent());
                }
            });
            log.info("Mapping completed for EngagementInstance {}", dataSources);
            String startValue = "jsonPath:";
            Map<String, Object> pathParams = apiUrl.getPathParams();
            Map<String, String> pathParamsMap = new HashMap<>();
            for (Map.Entry pathParamsEntry : pathParams.entrySet()) {
                Object pathElementValue = pathParamsEntry.getValue();
                if (pathElementValue instanceof String) {
                    String value = pathElementValue.toString();
                    if (value.startsWith(startValue)) {
                        String key = pathParamsEntry.getKey().toString();
                        String outputValue = processString(value);
                        pathParamsMap.put(key, outputValue);
                    } else {
                        String Key = pathParamsEntry.getKey().toString();

                        pathParamsMap.put(Key, value);
                    }
                }
            }
            log.info("Resolved values of pathParams {}", pathParamsMap);
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            Map<String, List<String>> queryParams = apiUrl.getQueryParams();
            HashMap<String, List<String>> queryParamsResolved = new HashMap<String, List<String>>();
            queryParams.entrySet().forEach(queryParamsEntry -> {
                String key = (String) queryParamsEntry.getKey();
                List<String> list = (List<String>) queryParamsEntry.getValue();
                for (int index = 0; index < list.size(); index++) {
                    String indexValue = list.get(index);
                    if (indexValue.startsWith(startValue)) {
                        String outputValue= processString(indexValue);
                        list.set(index, outputValue);
                        queryParamsResolved.put(key, list);
                    } else {
                        queryParamsResolved.put(key, list);
                    }
                }
            });

            queryParamsResolved.entrySet().forEach(entry -> {
                String key = (String) entry.getKey();
                List<String> value = (List<String>) entry.getValue();
                builder.queryParam(key, value);
            });
            URI uri = builder.buildAndExpand(pathParamsMap).toUri();
            log.info("Resolved value of URL {}", uri);
            Map<String, Object> body = apiUrl.getBody();
            Map<String, Object> resolvedBody = new HashMap<String, Object>();
            body.entrySet().forEach(bodyEntry -> {
                Object bodyEntryValue = bodyEntry.getValue();
                if (bodyEntryValue instanceof List<?>) {
                    List<Object> list = (List<Object>) bodyEntryValue;
                    for (int count = 0; count < list.size(); count++) {
                        Object listElement = list.get(count);
                        if (listElement instanceof String) {
                            String listElementValue = listElement.toString();
                            if (listElementValue.startsWith(startValue)) {
                                String outputValue = processString(listElementValue);
                                list.set(count, outputValue);
                            }
                        }

                    }
                } else if (bodyEntryValue instanceof String) {
                    String bodyElementValue = bodyEntryValue.toString();
                    if (bodyElementValue.startsWith(startValue)) {

                        String bodyStringValue = bodyElementValue.split(":")[1];
                        String bodyArray[] = bodyStringValue.replace(".", ":").split(":");
                        if (bodyArray.length == 1) {
                            Object instanceJson = dataSourcesMap.get(bodyStringValue);
                            String value = instanceJson.toString();
                            bodyEntry.setValue(value);
                        } else {
                            String bodyValue = bodyStringValue.replace(".", ":").split(":")[0];
                            Object instanceJson = dataSourcesMap.get(bodyValue);
                            String dataSourceValues[] = bodyStringValue.replace(".", ":").split(":");
                            Object parsed = JsonPath.parse(instanceJson.toString()).read(jsonPathExpressionFinder(dataSourceValues), Object.class);
                            String outputValue = parsed.toString();
                            bodyEntry.setValue(outputValue);
                        }
                    }
                } else if (bodyEntryValue instanceof Map) {
                    Map<String, Object> bodyEntryMap = (Map<String, Object>) bodyEntryValue;
                    resolveMap(bodyEntryMap);
                }
            });
            restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(uri,
                    HttpMethod.valueOf(apiUrl.getHttpMethod().toString()), new HttpEntity(apiUrl.getBody()),
                    String.class);
        }
    }

    private String processString(String value) {
        String paramValue = value.split(":")[1];
        String parameterValue = paramValue.replace(".", ":").split(":")[0];
        Object instanceJson = dataSourcesMap.get(parameterValue);
        String dataSourceValues[] = paramValue.replace(".", ":").split(":");
        Object parsedValue = JsonPath.parse(instanceJson.toString()).read(jsonPathExpressionFinder(dataSourceValues), Object.class);
        String outputValue = parsedValue.toString();
        return outputValue;
    }

    public void resolveMap(Map<String, Object> tempMap) {
        tempMap.entrySet().forEach(entry -> {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof String) {
                String valueString = value.toString();
                if ((valueString).startsWith("jsonPath:")) {
                    String outputValue = processString(valueString);
                    tempMap.put(key, outputValue);
                }
            } else if (value instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) value;
                resolveMap(inputMap);
            }
        });
    }

    public String jsonPathExpressionFinder(String data[]) {
        String jsonPathExpression = "";
        jsonPathExpression = jsonPathExpression + data[1];
        for (int index = 2; index < data.length; index++) {
            jsonPathExpression = jsonPathExpression + "." + data[index];
        }
        return jsonPathExpression;
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

            log.info("Triggering generation of async contents for api {}", engagement.getId());

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
 * Downloads an api package from a completed transaction of an api
 * request
 *
 * @param tenantId      ID of tenant
 * @param transactionId ID of transaction
 * @param engagementId  ID of engagement
 * @return api package
 */
