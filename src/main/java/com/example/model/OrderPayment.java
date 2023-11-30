package com.example.model;

import java.io.Serializable;
import java.lang.String;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_payments")
public class OrderPayment extends TimeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "paid", nullable = false)
	private Double paid;

	@Column(name = "method", nullable = false)
	private String method;

	@Column(name = "paid_at", nullable = false)
	private Timestamp paidAt;

	@Column(name = "order_id", nullable = false)
	private Long orderId;

	// 取引先会社リレーション
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, insertable = false, updatable = false, name = "order_id")
	@JsonIgnore
	private Order order;
}
