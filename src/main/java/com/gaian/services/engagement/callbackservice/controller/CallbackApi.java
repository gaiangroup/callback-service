package com.gaian.services.engagement.callbackservice.controller;

import org.springframework.http.ResponseEntity;

import com.gaian.services.engagement.callbackservice.model.TriggerResponse;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.request.channel.CallbackUrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gaian.services.engagement.model.DeliveryRequest;

@Api(value = "CallbackApiController", description = "the CallbackApiController API")
public interface CallbackApi {
	/**
     
     * @param tenantId ID of the tenant
     * @param inputRequest callback request
     * @return Response of the trigger
     */
	@ApiOperation(value = "Send callback engagement", notes = "Sends callback",
            response = TriggerResponse.class, tags={ "callback-api-controller", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "callback sent successfully", response = TriggerResponse.class),
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found") })
    @RequestMapping(value = "/v1.0/{tenantId}/callback",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
	ResponseEntity<TriggerResponse> sendCallbackEngagementUsingPOST(
            @ApiParam(value = "inputRequest" ,required=true )  @RequestBody DeliveryRequest inputRequest,
            @ApiParam(value = "tenantId",required=true ) @PathVariable("tenantId") String tenantId,
			@RequestHeader(value = "Accept", required = false) String accept);
	

}
