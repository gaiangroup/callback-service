package com.gaian.services.engagement.callbackservice.error.exception;

import static com.gaian.services.engagement.callback.error.CallbackErrors.CALLBACK_DELIVERY_FAILURE;

import com.gaian.services.exception.ApplicationException;

public class CallbackDeliveryException extends ApplicationException{
	
	public CallbackDeliveryException() {
        super(CALLBACK_DELIVERY_FAILURE);
    }

    public CallbackDeliveryException(String errorMessage) {
        super(CALLBACK_DELIVERY_FAILURE, errorMessage);
    }

    public CallbackDeliveryException(Throwable throwable) {
        super(CALLBACK_DELIVERY_FAILURE, throwable);
    }

    public CallbackDeliveryException(String errorMessage, Throwable throwable) {
        super(CALLBACK_DELIVERY_FAILURE, errorMessage, throwable);
    }

}
