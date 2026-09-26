package com.aditya.stockmanager.dto;

import jakarta.validation.constraints.Min;

public record StockAdjustmentRequest(
		@Min(value = 1, message = "quantity must be greater than zero") int quantity) {
}
