package com.aditya.stockmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aditya.stockmanager.domain.Product;
import com.aditya.stockmanager.dto.CreateProductRequest;
import com.aditya.stockmanager.exception.DuplicateProductNameException;
import com.aditya.stockmanager.exception.ProductNotDeletableException;
import com.aditya.stockmanager.exception.ProductNotFoundException;
import com.aditya.stockmanager.repository.ProductRepository;

@Service
public class ProductService {

	private static final int DEFAULT_REORDER_LEVEL = 5;

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public Product findById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
	}

	public Product create(CreateProductRequest request) {
		productRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
			throw new DuplicateProductNameException(request.name());
		});

		Product product = Product.builder().name(request.name()).quantity(request.quantity())
				.reorderLevel(request.reorderLevel() != null ? request.reorderLevel() : DEFAULT_REORDER_LEVEL)
				.build();
		return productRepository.save(product);
	}

	public void delete(Long id) {
		Product product = findById(id);
		if (product.getQuantity() != 0) {
			throw new ProductNotDeletableException(product.getName(), product.getQuantity());
		}
		productRepository.deleteById(id);
	}

}
