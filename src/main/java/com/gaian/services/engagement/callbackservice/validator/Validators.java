package com.gaian.services.engagement.callbackservice.validator;

import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.request.channel.CallbackUrl;

//import com.gaian.services.engagement.mef.validator.DeliveryRequest;

public interface Validators {
	
	void validateCallbackRequest(DeliveryRequest inputPayload);

	
}
