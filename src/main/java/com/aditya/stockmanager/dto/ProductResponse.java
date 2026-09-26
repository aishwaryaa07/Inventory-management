package com.aditya.stockmanager.dto;

import com.aditya.stockmanager.domain.Product;

public record ProductResponse(Long id, String name, int quantity, int reorderLevel, boolean lowStock) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getQuantity(),
				product.getReorderLevel(), product.isLowStock());
	}
}
