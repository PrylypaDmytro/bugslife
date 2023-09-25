package com.example.model;

import java.io.Serializable;
import java.lang.String;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_deliveries")
@Data
@Component
@Getter
@Setter
@NoArgsConstructor
public class OrderDelivery extends TimeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id", nullable = false)
	private Integer orderId;

	@Column(name = "shipping_code", nullable = false)
	private String shippingCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "shipping_date")
	private Date shippingDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "delivery_timezone")
	private String deliveryTimezone;

	@Transient
	private boolean checked = false;

	@Transient
	private String uploadStatus;

	public OrderDelivery(Integer orderId, String shippingCode, Date shippingDate, Date deliveryDate,
			String deliveryTimezone, boolean checked, String uploadStatus) {
		this.orderId = orderId;
		this.shippingCode = shippingCode;
		this.shippingDate = shippingDate;
		this.deliveryDate = deliveryDate;
		this.deliveryTimezone = deliveryTimezone;
		this.checked = checked;
		this.uploadStatus = uploadStatus;
	}

}
