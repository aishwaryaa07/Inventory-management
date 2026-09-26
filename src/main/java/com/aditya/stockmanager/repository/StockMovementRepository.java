package com.aditya.stockmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.stockmanager.domain.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

	List<StockMovement> findAllByOrderByOccurredAtDesc();

	List<StockMovement> findByProduct_IdOrderByOccurredAtDesc(Long productId);

}
