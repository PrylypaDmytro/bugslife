package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Order;
import com.example.model.OrderPayment;

public interface PaymentAmountRepository extends JpaRepository<OrderPayment, Long> {
	List<OrderPayment> findByOrder(Order order);
}
