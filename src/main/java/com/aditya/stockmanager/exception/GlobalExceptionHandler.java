package com.aditya.stockmanager.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aditya.stockmanager.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
		return ResponseEntity.status(ex.getStatus())
				.body(ApiError.of(ex.getStatus().value(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.joining("; "));
		return ResponseEntity.badRequest()
				.body(ApiError.of(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		logger.warn("Data integrity violation: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiError.of(HttpStatus.CONFLICT.value(),
						"This action conflicts with existing data (e.g. related records that still reference it).",
						request.getRequestURI()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
		logger.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError
				.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.", request.getRequestURI()));
	}

}
