package com.aditya.stockmanager.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ApiException {
	private static final long serialVersionUID = 1L;

	public InsufficientStockException(String productName, int available, int requested) {
		super("Cannot remove " + requested + " unit(s) of '" + productName + "': only " + available
				+ " unit(s) in stock");
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
