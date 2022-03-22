package com.gaian.services.engagement.callbackservice.error.exception;

import static com.gaian.services.engagement.callback.error.CallbackErrors.INVALID_CALLBACK_REQUEST;

import com.gaian.services.error.Error;
import com.gaian.services.exception.ValidationException;



public class InvalidCallbackRequestException extends ValidationException{
	public InvalidCallbackRequestException() {
        super(INVALID_CALLBACK_REQUEST);
    }

 public InvalidCallbackRequestException(String errorMessage) {
        super(INVALID_CALLBACK_REQUEST, errorMessage);
    }

    public InvalidCallbackRequestException(Throwable throwable) {
        super(INVALID_CALLBACK_REQUEST, throwable);
    }

    public InvalidCallbackRequestException(String errorMessage, Throwable throwable) {
        super(INVALID_CALLBACK_REQUEST, errorMessage, throwable);
    }


}
