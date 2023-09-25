package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByStatus(String status);

	Optional<Order> findById(Integer orderId);
}
