package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.OrderDelivery;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

	Optional<OrderDelivery> findByOrderIdOrShippingCode(Integer orderId, String shippingCode);
}
