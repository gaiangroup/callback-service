package com.gaian.services.engagement.apiservice.service;

import org.springframework.core.io.FileSystemResource;
import com.gaian.services.engagement.service.ChannelProcessor;

public interface ApiProcessor extends ChannelProcessor{
	
	FileSystemResource downloadPackage(String tenantId, String transactionId, String engagementId);
	
	
}
