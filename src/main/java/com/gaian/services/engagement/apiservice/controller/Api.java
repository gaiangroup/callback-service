package com.gaian.services.engagement.apiservice.controller;

import org.springframework.http.ResponseEntity;

import com.gaian.services.engagement.apiservice.model.TriggerResponse;
import com.gaian.services.engagement.model.DeliveryRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

@io.swagger.annotations.Api(value = "ApiController", description = "the ApiController API")
public interface Api {
	/**
     
     * @param tenantId ID of the tenant
     * @param inputRequest api request
     * @return Response of the trigger
     */
	@ApiOperation(value = "Send api engagement", notes = "Sends api",
            response = TriggerResponse.class, tags={ "api-controller", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "api sent successfully", response = TriggerResponse.class),
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found") })
    @RequestMapping(value = "/v1.0/{tenantId}/api",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
	ResponseEntity<TriggerResponse> sendApiEngagementUsingPOST(
            @ApiParam(value = "inputRequest" ,required=true )  @RequestBody DeliveryRequest inputRequest,
            @ApiParam(value = "tenantId",required=true ) @PathVariable("tenantId") String tenantId,
			@RequestHeader(value = "Accept", required = false) String accept);
	

}
