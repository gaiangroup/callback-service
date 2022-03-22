package com.gaian.services.engagement.callbackservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiParam;
import com.gaian.services.engagement.callbackservice.model.TriggerResponse;
import com.gaian.services.engagement.callbackservice.service.CallbackUrlProcessor;
import com.gaian.services.engagement.callbackservice.validator.Validators;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.request.channel.CallbackUrl;

import static org.springframework.http.ResponseEntity.ok;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CallbackApiController implements CallbackApi{
	
	@Autowired
    private Validators validators;
	
	@Autowired
	private CallbackUrlProcessor callbackUrlProcessor;


	/*
	 * @Override public ResponseEntity<TriggerResponse>
	 * sendCallbackEngagementUsingPOST( @ApiParam(value = "inputRequest"
	 * ,required=true )@RequestBody CallbackUrl inputRequest) {
	 * 
	 * log.info("*** New Callback url request for request : {}", inputRequest);
	 * 
	 * //validators.validateCallbackRequest(inputRequest);
	 * callbackUrlProcessor.initiateEngagementProcessing(inputRequest);
	 * 
	 * return ok(new TriggerResponse("Callback posted successful"));
	 * 
	 * }
	 */
	
	//@Override
    public ResponseEntity<TriggerResponse> sendCallbackEngagementUsingPOST(
            @ApiParam(value = "inputRequest" ,required=true )  @RequestBody DeliveryRequest inputRequest,
            @ApiParam(value = "appId",required=true ) @PathVariable("appId") String appId,
            @RequestHeader(value = "Accept", required = false) String accept) {

        log.info("*** New callback engagement request for tenant {} : {}", appId, inputRequest);

        validators.validateCallbackRequest(inputRequest);
        callbackUrlProcessor.initiateEngagementProcessing(inputRequest);

        return ok(new TriggerResponse(
            inputRequest.getEngagement().getId(), inputRequest.getTransactionId()));
    }
}
