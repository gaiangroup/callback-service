package com.gaian.services.engagement.callbackservice.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
//
//import com.gaian.services.engagement.mef.model.Data;
//import com.gaian.services.engagement.mef.model.DataSource;
//import com.gaian.services.engagement.mef.model.JsonInclude;
//import com.gaian.services.engagement.mef.model.Metadata;
//import com.gaian.services.engagement.mef.model.NoArgsConstructor;
//import com.gaian.services.engagement.mef.model.UploadRequest;
import com.gaian.services.engagement.model.Communication;
import com.gaian.services.engagement.model.Targetting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadRequest {
	
	 private String requestId;
	    private String tenantId;
	    private List<DataSource> datasources;
	    private Long expiryAt;
	    private String priority;
	    private Communication communication;
	    private Targetting targeting;
	    private Metadata metadata;

}
