package com.example.model;

import java.io.Serializable;
import java.lang.String;
import java.util.Date;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	@Column(name = "shipping_date")
	private Date shippingDate;

	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "delivery_timezone")
	private String deliveryTimezone;

	@Column(name = "status")
	private String status;

	private boolean checked;

	private String uploadStatus;

	public OrderDelivery(Integer orderId, String shippingCode, Date shippingDate, Date deliveryDate,
			String deliveryTimezone, String status, boolean checked, String uploadStatus) {
		this.orderId = orderId;
		this.shippingCode = shippingCode;
		this.shippingDate = shippingDate;
		this.deliveryDate = deliveryDate;
		this.deliveryTimezone = deliveryTimezone;
		this.status = status;
		this.checked = checked;
		this.uploadStatus = uploadStatus;
	}

}
