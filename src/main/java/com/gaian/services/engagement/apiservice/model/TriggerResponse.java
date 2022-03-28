package com.gaian.services.engagement.apiservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.gaian.services.engagement.mef.model.Data;
//import com.gaian.services.engagement.mef.model.JsonIgnoreProperties;
//import com.gaian.services.engagement.mef.model.TriggerResponse;
import com.gaian.services.tfw.Status;



@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerResponse {
	
	 private Status status;

	    private String message;

	    private String engagementId;

	    private String transactionId;
	    
	    public TriggerResponse() {
	        status =Status.SUCCESS;
	        this.message = "Successfully initiated mef processing";
	    }
	    
	    public TriggerResponse(String message) {
	        this();
	        this.message = message;
	    }
	    
	    public TriggerResponse(String engagementId, String transactionId) {
	        this();
	        this.engagementId = engagementId;
	        this.transactionId = transactionId;
	    }


}
