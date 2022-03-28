package com.gaian.services.engagement.apiservice.error.exception;

import static com.gaian.services.engagement.api.error.ApiErrors.INVALID_API_REQUEST;

import com.gaian.services.exception.ValidationException;



public class InvalidApiRequestException extends ValidationException{
	public InvalidApiRequestException() {
        super(INVALID_API_REQUEST);
    }

 public InvalidApiRequestException(String errorMessage) {
        super(INVALID_API_REQUEST, errorMessage);
    }

    public InvalidApiRequestException(Throwable throwable) {
        super(INVALID_API_REQUEST, throwable);
    }

    public InvalidApiRequestException(String errorMessage, Throwable throwable) {
        super(INVALID_API_REQUEST, errorMessage, throwable);
    }


}
