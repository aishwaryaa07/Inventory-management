package com.aditya.stockmanager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aditya.stockmanager.domain.MovementType;
import com.aditya.stockmanager.domain.Product;
import com.aditya.stockmanager.domain.StockMovement;
import com.aditya.stockmanager.exception.InsufficientStockException;
import com.aditya.stockmanager.exception.ProductNotFoundException;
import com.aditya.stockmanager.repository.ProductRepository;
import com.aditya.stockmanager.repository.StockMovementRepository;

@Service
public class StockService {

	private final ProductRepository productRepository;
	private final StockMovementRepository movementRepository;

	public StockService(ProductRepository productRepository, StockMovementRepository movementRepository) {
		this.productRepository = productRepository;
		this.movementRepository = movementRepository;
	}

	@Transactional
	public Product stockIn(Long productId, int quantity) {
		Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));

		product.setQuantity(product.getQuantity() + quantity);
		Product saved = productRepository.save(product);
		recordMovement(saved, quantity, MovementType.STOCK_IN);
		return saved;
	}

	@Transactional
	public Product stockOut(Long productId, int quantity) {
		Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));

		if (product.getQuantity() < quantity) {
			throw new InsufficientStockException(product.getName(), product.getQuantity(), quantity);
		}

		product.setQuantity(product.getQuantity() - quantity);
		Product saved = productRepository.save(product);
		recordMovement(saved, quantity, MovementType.STOCK_OUT);
		return saved;
	}

	public List<StockMovement> findAllMovements() {
		return movementRepository.findAllByOrderByOccurredAtDesc();
	}

	public List<StockMovement> findMovementsForProduct(Long productId) {
		return movementRepository.findByProduct_IdOrderByOccurredAtDesc(productId);
	}

	private void recordMovement(Product product, int quantity, MovementType type) {
		movementRepository.save(StockMovement.builder().product(product).quantityChange(quantity).type(type)
				.occurredAt(LocalDateTime.now()).build());
	}

}
