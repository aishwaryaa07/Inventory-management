package com.aditya.stockmanager.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	protected ApiException(String message) {
		super(message);
	}

	public abstract HttpStatus getStatus();
}
