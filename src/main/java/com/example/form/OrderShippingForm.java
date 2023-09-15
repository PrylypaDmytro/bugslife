package com.example.form;

import java.util.ArrayList;
import java.util.List;

import com.example.model.OrderDelivery;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderShippingForm {
	@NotEmpty(message = "必ず一つは選択してください。")
	private List<Boolean> checkedList = new ArrayList<Boolean>();
	// private CampaignStatus nextStatus = CampaignStatus.valueOf(0);

	private List<OrderDelivery> orderShippingList;

	// Getter and setter for orderShippingList
	public List<OrderDelivery> getOrderShippingList() {
		return orderShippingList;
	}

	public void setOrderShippingList(List<OrderDelivery> orderShippingList) {
		this.orderShippingList = orderShippingList;
	}
}
