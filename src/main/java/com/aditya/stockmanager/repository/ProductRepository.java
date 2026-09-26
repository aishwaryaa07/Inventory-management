package com.aditya.stockmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.stockmanager.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByNameIgnoreCase(String name);

}
