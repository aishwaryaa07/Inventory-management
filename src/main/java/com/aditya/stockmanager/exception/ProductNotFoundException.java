package com.aditya.stockmanager.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ProductNotFoundException(Long id) {
		super("Product " + id + " was not found");
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.NOT_FOUND;
	}
}
