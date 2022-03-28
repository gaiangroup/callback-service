package com.gaian.services.engagement.apiservice.error.exception;

import static com.gaian.services.engagement.api.error.ApiErrors.API_DELIVERY_FAILURE;

import com.gaian.services.exception.ApplicationException;

public class ApiDeliveryException extends ApplicationException{
	
	public ApiDeliveryException() {
        super(API_DELIVERY_FAILURE);
    }

    public ApiDeliveryException(String errorMessage) {
        super(API_DELIVERY_FAILURE, errorMessage);
    }

    public ApiDeliveryException(Throwable throwable) {
        super(API_DELIVERY_FAILURE, throwable);
    }

    public ApiDeliveryException(String errorMessage, Throwable throwable) {
        super(API_DELIVERY_FAILURE, errorMessage, throwable);
    }

}
