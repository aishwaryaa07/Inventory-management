package com.aditya.stockmanager.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.stockmanager.dto.CreateProductRequest;
import com.aditya.stockmanager.dto.ProductResponse;
import com.aditya.stockmanager.dto.StockAdjustmentRequest;
import com.aditya.stockmanager.dto.StockMovementResponse;
import com.aditya.stockmanager.service.ProductService;
import com.aditya.stockmanager.service.StockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;
	private final StockService stockService;

	public ProductController(ProductService productService, StockService stockService) {
		this.productService = productService;
		this.stockService = stockService;
	}

	@GetMapping
	public List<ProductResponse> getAll() {
		return productService.findAll().stream().map(ProductResponse::from).toList();
	}

	@GetMapping("/{id}")
	public ProductResponse getOne(@PathVariable Long id) {
		return ProductResponse.from(productService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
		return ProductResponse.from(productService.create(request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		productService.delete(id);
	}

	@PostMapping("/{id}/stock-in")
	public ProductResponse stockIn(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
		return ProductResponse.from(stockService.stockIn(id, request.quantity()));
	}

	@PostMapping("/{id}/stock-out")
	public ProductResponse stockOut(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
		return ProductResponse.from(stockService.stockOut(id, request.quantity()));
	}

	@GetMapping("/{id}/transactions")
	public List<StockMovementResponse> getTransactionsForProduct(@PathVariable Long id) {
		productService.findById(id);
		return stockService.findMovementsForProduct(id).stream().map(StockMovementResponse::from).toList();
	}

}
