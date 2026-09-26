package com.aditya.stockmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aditya.stockmanager.domain.Product;
import com.aditya.stockmanager.dto.CreateProductRequest;
import com.aditya.stockmanager.exception.DuplicateProductNameException;
import com.aditya.stockmanager.exception.ProductNotDeletableException;
import com.aditya.stockmanager.exception.ProductNotFoundException;
import com.aditya.stockmanager.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	private ProductService productService;

	@Test
	void findAll_returnsEveryProduct() {
		productService = new ProductService(productRepository);
		Product laptop = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		Product mouse = Product.builder().id(2L).name("Mouse").quantity(3).reorderLevel(5).build();
		when(productRepository.findAll()).thenReturn(List.of(laptop, mouse));

		assertEquals(2, productService.findAll().size());
	}

	@Test
	void findById_throwsWhenMissing() {
		productService = new ProductService(productRepository);
		when(productRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class, () -> productService.findById(99L));
	}

	@Test
	void create_rejectsDuplicateName() {
		productService = new ProductService(productRepository);
		Product existing = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		when(productRepository.findByNameIgnoreCase("Laptop")).thenReturn(Optional.of(existing));

		CreateProductRequest request = new CreateProductRequest("Laptop", 5, null);

		assertThrows(DuplicateProductNameException.class, () -> productService.create(request));
	}

	@Test
	void create_defaultsReorderLevelWhenNotProvided() {
		productService = new ProductService(productRepository);
		when(productRepository.findByNameIgnoreCase("Keyboard")).thenReturn(Optional.empty());
		when(productRepository.save(any(Product.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Product created = productService.create(new CreateProductRequest("Keyboard", 20, null));

		assertEquals(5, created.getReorderLevel());
		assertEquals("Keyboard", created.getName());
	}

	@Test
	void delete_rejectsWhenQuantityRemains() {
		productService = new ProductService(productRepository);
		Product product = Product.builder().id(1L).name("Laptop").quantity(3).reorderLevel(5).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		assertThrows(ProductNotDeletableException.class, () -> productService.delete(1L));
	}

	@Test
	void delete_removesProductWhenQuantityIsZero() {
		productService = new ProductService(productRepository);
		Product product = Product.builder().id(1L).name("Laptop").quantity(0).reorderLevel(5).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		productService.delete(1L);

		verify(productRepository, times(1)).deleteById(1L);
	}

}
