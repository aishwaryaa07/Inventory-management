package com.aditya.stockmanager.exception;

import org.springframework.http.HttpStatus;

public class ProductNotDeletableException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ProductNotDeletableException(String productName, int quantity) {
		super("'" + productName + "' still has " + quantity + " unit(s) in stock and cannot be deleted");
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.CONFLICT;
	}
}
