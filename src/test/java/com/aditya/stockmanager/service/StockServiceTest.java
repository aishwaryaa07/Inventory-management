package com.aditya.stockmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aditya.stockmanager.domain.Product;
import com.aditya.stockmanager.domain.StockMovement;
import com.aditya.stockmanager.exception.InsufficientStockException;
import com.aditya.stockmanager.exception.ProductNotFoundException;
import com.aditya.stockmanager.repository.ProductRepository;
import com.aditya.stockmanager.repository.StockMovementRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockMovementRepository movementRepository;

	private StockService stockService;

	@Test
	void stockIn_increasesQuantityAndRecordsMovement() {
		stockService = new StockService(productRepository, movementRepository);
		Product product = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Product updated = stockService.stockIn(1L, 5);

		assertEquals(15, updated.getQuantity());
	}

	@Test
	void stockOut_decreasesQuantityWhenEnoughStock() {
		stockService = new StockService(productRepository, movementRepository);
		Product product = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Product updated = stockService.stockOut(1L, 4);

		assertEquals(6, updated.getQuantity());
	}

	@Test
	void stockOut_rejectsWhenNotEnoughStock() {
		stockService = new StockService(productRepository, movementRepository);
		Product product = Product.builder().id(1L).name("Laptop").quantity(2).reorderLevel(5).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		assertThrows(InsufficientStockException.class, () -> stockService.stockOut(1L, 5));
	}

	@Test
	void stockIn_rejectsWhenProductMissing() {
		stockService = new StockService(productRepository, movementRepository);
		when(productRepository.findById(42L)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class, () -> stockService.stockIn(42L, 5));
	}

}
