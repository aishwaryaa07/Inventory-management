package com.aditya.stockmanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(

		@NotBlank(message = "name is required") String name,

		@Min(value = 0, message = "quantity cannot be negative") int quantity,

		@Min(value = 0, message = "reorderLevel cannot be negative") Integer reorderLevel) {
}
