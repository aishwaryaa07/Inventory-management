package com.aditya.stockmanager.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.stockmanager.dto.StockMovementResponse;
import com.aditya.stockmanager.service.StockService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final StockService stockService;

	public TransactionController(StockService stockService) {
		this.stockService = stockService;
	}

	@GetMapping
	public List<StockMovementResponse> getAll() {
		return stockService.findAllMovements().stream().map(StockMovementResponse::from).toList();
	}

}
