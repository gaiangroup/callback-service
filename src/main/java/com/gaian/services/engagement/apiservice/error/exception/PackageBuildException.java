package com.gaian.services.engagement.apiservice.error.exception;

import static com.gaian.services.engagement.api.error.ApiErrors.PACKAGE_BUILD_FAILURE;

import com.gaian.services.exception.ApplicationException;

public class PackageBuildException extends ApplicationException {

	public PackageBuildException() {
		super(PACKAGE_BUILD_FAILURE);
	}

	public PackageBuildException(String errorMessage) {
		super(PACKAGE_BUILD_FAILURE, errorMessage);
	}

	public PackageBuildException(Throwable throwable) {
		super(PACKAGE_BUILD_FAILURE, throwable);
	}

	public PackageBuildException(String errorMessage, Throwable throwable) {
		super(PACKAGE_BUILD_FAILURE, errorMessage, throwable);

	}
}
