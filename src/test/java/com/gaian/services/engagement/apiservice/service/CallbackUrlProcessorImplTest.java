package com.gaian.services.engagement.apiservice.service;

import java.util.*;

import com.gaian.services.engagement.model.request.channel.ApiUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;

import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.db.EngagementInstance;
import com.gaian.services.engagement.model.request.DataSourceEnum;
//import com.gaian.services.engagement.model.request.channel.CallbackUrl;
import com.gaian.services.engagement.model.request.channel.ChannelType;
import com.gaian.services.engagement.model.request.content.DataSourceMapping;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CallbackUrlProcessorImplTest {

	/*@InjectMocks
	private RestTemplate restTemplate;*/

	@InjectMocks
	ApiProcessorImpl callbackUrlProcessorImpl;

	@SuppressWarnings("deprecation")
	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void resumeEngagementTest() {
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		List<String> l = new ArrayList<String>();
		l.add("application/json");
		headers.put("Content-Type", l);

		HashMap<String, Object> pathParams = new HashMap<String, Object>();
		pathParams.put("id", "jsonPath:trigger.app.name");
		pathParams.put("size", "100");

		HashMap<String, List<String>> queryParams = new HashMap<String, List<String>>();
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		List<String> l3 = new ArrayList<String>();
		l1.add("1");
		l2.add("jsonPath:trigger.app.sort");
		l3.add("3");
		queryParams.put("page", Arrays.asList("1"));
		queryParams.put("sort", Arrays.asList("jsonPath:trigger.app.sort"));
		queryParams.put("size", Arrays.asList("3"));

		HashMap<String, Object> body = new HashMap<String, Object>();
		List<Object> l4 = new ArrayList<Object>();
		l4.add(0);
		l4.add("jsonPath:source.tenant.id");
		l4.add(8);
		/*Map<String, String> element = new HashMap<String, String>();
		element.put("field1", "jsonPath:destination.data.field1");
		element.put("field2", "jsonPath:destination.data.field2");*/
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("field1", "jsonPath:destination.data.field1");
		fields.put("field2", "jsonPath:destination.data.field2");
		fields.put("field3", 100);

		Map<String, Map<String, Object>> element = new HashMap<String, Map<String, Object>>();
		element.put("fields", fields);
		body.put("count", 10);
		body.put("tenantIds", l4);
		body.put("id", "jsonPath:trigger");
		body.put("element", element);
		body.put("name", "jsonPath:source.emp.name");
		body.put("attr1", "value1");

		//setField(callbackUrlProcessorImpl, "restTemplate", restTemplate);




		DataSourceMapping obj1 = new DataSourceMapping();
		DataSourceMapping obj2 = new DataSourceMapping();
		DataSourceMapping obj3 = new DataSourceMapping();
		obj1.setKey("trigger");
		obj1.setValue(DataSourceEnum.TRIGGER);
		obj2.setKey("source");
		obj2.setValue(DataSourceEnum.SOURCE);
		obj3.setKey("destination");
		obj3.setValue(DataSourceEnum.DESTINATION);

		List<DataSourceMapping> dataSources = new ArrayList<DataSourceMapping>();
		dataSources.add(obj1);
		dataSources.add(obj2);
		dataSources.add(obj3);

		ApiUrl apiUrl = new ApiUrl();
		ApiUrl callbackChannel = new ApiUrl();
		apiUrl.setUrlType("REST");
		apiUrl.setUrl("https://ingress-gateway.gaiansolutions.com/darpa/v1.0/{id}/details/{size}");
		apiUrl.setHttpMethod(HttpMethod.POST);
		apiUrl.setHeaders(headers);
		apiUrl.setPathParams(pathParams);
		apiUrl.setQueryParams(queryParams);
		apiUrl.setBody(body);
		apiUrl.setDataSources(dataSources);
		apiUrl.getChannelType();

		ChannelType channelType = apiUrl.getChannelType();



		EngagementDBModel engagementDBModel = new EngagementDBModel();

		engagementDBModel.setChannel(apiUrl);

		/*
		 * Map<String, Object> sourceData = new HashMap<String, Object>(); Map<String,
		 * String> emp = new HashMap<String, String>(); Map<String, String> tenant = new
		 * HashMap<String, String>(); emp.put("name", "JackWilder"); tenant.put("name",
		 * "GAIAN"); tenant.put("id", "c09"); sourceData.put("url",
		 * "http://tf-web.default.svc.cluster.local:8080/v1.0/620b8bcac358ed0001464f1a/groups/620b8c09a8242e0001215692/data?size=100"
		 * ); sourceData.put("method", "POST"); sourceData.put("emp", emp);
		 * sourceData.put("tenant", tenant);
		 */

		String eventJson = "{\"app\":{\"name\":\"NodeApp\",\"sort\":\"DESC\"},\"queryId\":\"620b8c09a8242e0001215692\",\"tenantId\":\"620b8bcac358ed0001464f1a\",\"entityId\":\"CMRITmbcbjxhahh-mkqgothugs-JNTUHercgocuesn\",\"actionType\":\"ADDITION\",\"entities\":[{\"_id\":\"620b8e00a8242e000121ba08\",\"tenantID\":\"620b8bcac358ed0001464f1a\",\"entityID\":\"CMRITmbcbjxhahh-mkqgothugs-JNTUHercgocuesn\",\"transactionID\":null,\"entity\":{\"universityName\":\"JNTUHercgocuesn\",\"clgName\":\"CMRITmbcbjxhahh\",\"universityPinCode\":\"7474647\",\"universityAddress\":\"jdhdh\",\"clgAddress\":\"Kolkata\",\"clgPinCode\":\"7474647\",\"stdName\":\"mkqgothugs\",\"stdBranch\":\"CHE\",\"stdAge\":\"20\",\"stdRecordDate\":\"2022-02-15T16:56:56.063IST\",\"stdPassoutYear\":\"2020\",\"professors\":[\"mnznwo\",\"kzfwuxd\",\"cklkq\"]},\"creationDate\":\"1644924416812\",\"updationDate\":0}],\"notificationType\":\"GROUP_CHANGED\"}";
		String sourceJson = "{\"url\":\"http://tf-web.default.svc.cluster.local:8080/v1.0/620b8bcac358ed0001464f1a/groups/620b8c09a8242e0001215692/data?size=100\",\"method\":\"POST\",\"emp\":{\"name\":\"JackWilder\"},\"tenant\":{\"name\":\"GAIAN\",\"id\":\"c09\"}}";
		String destinationJson = "{\"userID\":\"manoj.sadhu943@gaiansolutions.com\",\"userName\":\"Manoj Sadhu\",\"userCity\":\"Hyderabad\",\"usergender\":\"Male\",\"emailAddr\":\"manoj.sadhu943@gaiansolutions.com\",\"_id\":\"manoj.sadhu943@gaiansolutions.com\",\"data\":{\"field1\":{\"name\":\"f1\",\"id\":12345},\"field2\":{\"name\":\"f2\",\"id\":54321}}}";

		EngagementInstance engagementInstance = new EngagementInstance();
		engagementInstance.setSourceData(sourceJson);
		engagementInstance.setDestData(destinationJson);
		engagementInstance.setEvent(eventJson);

		callbackUrlProcessorImpl.resumeEngagement(engagementDBModel, engagementInstance);

	}

}

