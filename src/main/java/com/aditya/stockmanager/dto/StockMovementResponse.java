package com.aditya.stockmanager.dto;

import java.time.LocalDateTime;

import com.aditya.stockmanager.domain.MovementType;
import com.aditya.stockmanager.domain.StockMovement;

public record StockMovementResponse(Long id, Long productId, String productName, int quantityChange,
		MovementType type, LocalDateTime occurredAt) {

	public static StockMovementResponse from(StockMovement movement) {
		return new StockMovementResponse(movement.getId(), movement.getProduct().getId(),
				movement.getProduct().getName(), movement.getQuantityChange(), movement.getType(),
				movement.getOccurredAt());
	}
}
