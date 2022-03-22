package com.gaian.services.engagement.callbackservice.service;

import com.gaian.services.engagement.model.db.EngagementInstance;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

public class Practice {


    public static void main(String[] args) {
        String matchingValue = "jsonPath:trigger.app.name";
        String tempParameterValue = matchingValue.split(":")[1];

        String tempDataSource[] = tempParameterValue.replace(".", ":").split(":");
        String jsonPathExpression = "";
        jsonPathExpression = jsonPathExpression + tempDataSource[1];
        for (int i = 2; i < tempDataSource.length; i++) {
            jsonPathExpression = jsonPathExpression + "." + tempDataSource[i];
        }

        String parameterValue = tempParameterValue.replace(".", ":").split(":")[0];

        String json ="{\"app\":{\"name\":\"NodeApp\",\"sort\":\"DESC\"},\"queryId\":\"620b8c09a8242e0001215692\",\"tenantId\":\"620b8bcac358ed0001464f1a\",\"entityId\":\"CMRITmbcbjxhahh-mkqgothugs-JNTUHercgocuesn\",\"actionType\":\"ADDITION\",\"entities\":[{\"_id\":\"620b8e00a8242e000121ba08\",\"tenantID\":\"620b8bcac358ed0001464f1a\",\"entityID\":\"CMRITmbcbjxhahh-mkqgothugs-JNTUHercgocuesn\",\"transactionID\":null,\"entity\":{\"universityName\":\"JNTUHercgocuesn\",\"clgName\":\"CMRITmbcbjxhahh\",\"universityPinCode\":\"7474647\",\"universityAddress\":\"jdhdh\",\"clgAddress\":\"Kolkata\",\"clgPinCode\":\"7474647\",\"stdName\":\"mkqgothugs\",\"stdBranch\":\"CHE\",\"stdAge\":\"20\",\"stdRecordDate\":\"2022-02-15T16:56:56.063IST\",\"stdPassoutYear\":\"2020\",\"professors\":[\"mnznwo\",\"kzfwuxd\",\"cklkq\"]},\"creationDate\":\"1644924416812\",\"updationDate\":0}],\"notificationType\":\"GROUP_CHANGED\"}";




        Object parsed = JsonPath.parse(json).read(jsonPathExpression, Object.class);

        System.out.println(parsed.toString());


    }
}
