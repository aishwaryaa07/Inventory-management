package com.aditya.stockmanager.dto;

import java.time.LocalDateTime;

public record ApiError(int status, String message, String path, LocalDateTime timestamp) {

	public static ApiError of(int status, String message, String path) {
		return new ApiError(status, message, path, LocalDateTime.now());
	}
}
