package com.gaian.services.engagement.apiservice.validator;

import com.gaian.services.engagement.model.DeliveryRequest;

//import com.gaian.services.engagement.mef.validator.DeliveryRequest;

public interface Validators {
	
	void validateApiRequest(DeliveryRequest inputPayload);

	
}
