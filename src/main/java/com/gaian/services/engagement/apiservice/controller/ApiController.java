package com.gaian.services.engagement.apiservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiParam;
import com.gaian.services.engagement.apiservice.model.TriggerResponse;
import com.gaian.services.engagement.apiservice.service.ApiProcessor;
import com.gaian.services.engagement.apiservice.validator.Validators;
import com.gaian.services.engagement.model.DeliveryRequest;

import static org.springframework.http.ResponseEntity.ok;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ApiController implements Api {
	
	@Autowired
    private Validators validators;


	@Autowired
	private ApiProcessor ApiUrlProcessor;


	/*
	 * @Override public ResponseEntity<TriggerResponse>
	 * sendApiEngagementUsingPOST( @ApiParam(value = "inputRequest"
	 * ,required=true )@RequestBody ApiUrl inputRequest) {
	 * 
	 * log.info("*** New Api url request for request : {}", inputRequest);
	 * 
	 * //validators.validateApiRequest(inputRequest);
	 * ApiUrlProcessor.initiateEngagementProcessing(inputRequest);
	 * 
	 * return ok(new TriggerResponse("Api posted successful"));
	 * 
	 * }
	 */
	
	//@Override
    public ResponseEntity<TriggerResponse> sendApiEngagementUsingPOST(
            @ApiParam(value = "inputRequest" ,required=true )  @RequestBody DeliveryRequest inputRequest,
            @ApiParam(value = "appId",required=true ) @PathVariable("appId") String appId,
            @RequestHeader(value = "Accept", required = false) String accept) {

        log.info("*** New api engagement request for tenant {} : {}", appId, inputRequest);

        validators.validateApiRequest(inputRequest);
        ApiUrlProcessor.initiateEngagementProcessing(inputRequest);

        return ok(new TriggerResponse(
            inputRequest.getEngagement().getId(), inputRequest.getTransactionId()));
    }
}
