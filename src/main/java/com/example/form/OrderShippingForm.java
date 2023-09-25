package com.example.form;

import java.util.ArrayList;
import java.util.List;

import com.example.model.OrderDelivery;

// import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderShippingForm {

	private List<OrderDelivery> orderShippingList;

	// Getter and setter for orderShippingList
	public List<OrderDelivery> getOrderShippingList() {
		return orderShippingList;
	}

	public void setOrderShippingList(List<OrderDelivery> orderShippingList) {
		this.orderShippingList = orderShippingList;
	}
}
