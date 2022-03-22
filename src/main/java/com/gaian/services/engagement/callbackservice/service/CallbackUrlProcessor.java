package com.gaian.services.engagement.callbackservice.service;

import org.springframework.core.io.FileSystemResource;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.service.ChannelProcessor;
import java.util.concurrent.Future;
import org.springframework.core.io.FileSystemResource;
import com.gaian.services.engagement.service.ChannelProcessor;

public interface CallbackUrlProcessor extends ChannelProcessor{
	
	FileSystemResource downloadPackage(String tenantId, String transactionId, String engagementId);
	
	
}
