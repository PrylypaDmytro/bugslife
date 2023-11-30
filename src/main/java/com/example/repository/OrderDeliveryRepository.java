package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.model.OrderDelivery;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

	Optional<OrderDelivery> findByOrderIdOrShippingCode(Integer orderId, String shippingCode);

	@Query("SELECT od FROM OrderDelivery od WHERE od.order.id IN :orderIds")
	List<OrderDelivery> findAllByOrderIds(@Param("orderIds") List<Long> orderIds);

}
