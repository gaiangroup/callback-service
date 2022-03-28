package com.gaian.services.engagement.api.error;

import com.gaian.services.error.CommonErrors;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import com.gaian.services.error.Error;

public class ApiErrors extends CommonErrors{
	
	public static final Error INVALID_API_REQUEST = new Error(
	        BAD_REQUEST,
	        4001,
	        "The Api request is invalid ",
	        "Kindly verify if your request is valid or contact the support team"
	    );
	
	public static final Error API_DELIVERY_FAILURE = new Error(
	        INTERNAL_SERVER_ERROR,
	        5001,
	        "Failed to deliver the Api to the requested devices ",
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
	        "Failed to build Api package ",
	        "Kindly verify if the request is valid or contact the support team"
	    );

}
