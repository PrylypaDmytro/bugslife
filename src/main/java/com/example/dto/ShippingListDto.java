package com.example.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;
import com.example.model.OrderDelivery;

@Data
@Component
public class ShippingListDto {
	private List<OrderDelivery> orderShippingList;
}
