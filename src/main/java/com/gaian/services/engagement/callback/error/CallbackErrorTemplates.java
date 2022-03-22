package com.gaian.services.engagement.callback.error;

import com.gaian.services.engagement.error.ErrorTemplates;

/**
 * Templates for common error messages
 */

public class CallbackErrorTemplates extends ErrorTemplates{
	 private CallbackErrorTemplates() {
	        throw new IllegalStateException("Utility class");
	    }


	    public static final String ERROR_CALLBACK_PACKAGE_UPLOAD = "Error uploading package to pitcher: %s ";

	    public static final String ERROR_CALLBACK_DELIVERY = "Error sending MEF %s under transaction %s ";

	    public static final String ERROR_CONTENT_RESOLUTION = "Failed to resolve content for MEF %s under transaction %s ";

	    public static final String ERROR_DIRECTORY_CREATION = "Failed to create directory '%s' at '%s' ";

	    public static final String ERROR_AQ_UNSUPPORTED = "Analytics is not supported as source for MEF engagement %s";

	    public static final String ERROR_INVALID_CALLBACK_ENGAGEMENT = "Invalid MEF engagement ";

	    public static final String ERROR_INVALID_CALLBACK_CHANNEL = "Not an engagement of MEF channel ";

	    public static final String ERROR_BUILDING_PACKAGE = "Failed to build package %s for transaction %s ";

	    public static final String ERROR_POLLING_TOPIC = "Failed to poll messages from '%s' topic";

	    public static final String EMPTY_ASYNC_CONTENT_RESPONSE = "Empty async content received";
	}


