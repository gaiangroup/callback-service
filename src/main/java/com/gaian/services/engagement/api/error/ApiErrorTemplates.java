package com.gaian.services.engagement.api.error;

import com.gaian.services.engagement.error.ErrorTemplates;

/**
/**
 * Templates for common error messages
 */

public class ApiErrorTemplates extends ErrorTemplates{
	 private ApiErrorTemplates() {
	        throw new IllegalStateException("Utility class");
	    }


	    public static final String ERROR_API_PACKAGE_UPLOAD = "Error uploading package to pitcher: %s ";

	    public static final String ERROR_API_DELIVERY = "Error sending API %s under transaction %s ";

	    public static final String ERROR_API_RESOLUTION = "Failed to resolve content for API %s under transaction %s ";

	    public static final String ERROR_DIRECTORY_CREATION = "Failed to create directory '%s' at '%s' ";

	    public static final String ERROR_AQ_UNSUPPORTED = "Analytics is not supported as source for  engagement %s";

	    public static final String ERROR_INVALID_API_ENGAGEMENT = "Invalid API engagement ";

	    public static final String ERROR_INVALID_API_CHANNEL = "Not an engagement of API channel ";

	    public static final String ERROR_BUILDING_PACKAGE = "Failed to build package %s for transaction %s ";
	    public static final String ERROR_POLLING_TOPIC = "Failed to poll messages from '%s' topic";

	    public static final String EMPTY_ASYNC_CONTENT_RESPONSE = "Empty async content received";
	}


