package com.aditya.stockmanager.exception;

import org.springframework.http.HttpStatus;

public class DuplicateProductNameException extends ApiException {
	private static final long serialVersionUID = 1L;

	public DuplicateProductNameException(String name) {
		super("A product named '" + name + "' already exists");
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.CONFLICT;
	}
}
