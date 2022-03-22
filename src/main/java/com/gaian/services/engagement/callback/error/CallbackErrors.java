package com.gaian.services.engagement.callback.error;

import com.gaian.services.error.CommonErrors;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import com.gaian.services.error.Error;

public class CallbackErrors extends CommonErrors{
	
	public static final Error INVALID_CALLBACK_REQUEST = new Error(
	        BAD_REQUEST,
	        4001,
	        "The Callback request is invalid ",
	        "Kindly verify if your request is valid or contact the support team"
	    );
	
	public static final Error CALLBACK_DELIVERY_FAILURE = new Error(
	        INTERNAL_SERVER_ERROR,
	        5001,
	        "Failed to deliver the Callback to the requested devices ",
	        "Kindly verify if your request is valid or contact the support team"
	    );

	    public static final Error CONTENT_RESOLUTION_FAILURE = new Error(
	        INTERNAL_SERVER_ERROR,
	        5002,
	        "Failed to resolve one of the contents ",
	        "Kindly verify if content is resolvable or contact the support team"
	    );

	    public static final Error PACKAGE_BUILD_FAILURE = new Error(
	        INTERNAL_SERVER_ERROR,
	        5003,
	        "Failed to build Callback package ",
	        "Kindly verify if the request is valid or contact the support team"
	    );

}
