package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.constants.TaxType;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderForm.UpdateStatus;
import com.example.model.Order;
import com.example.model.OrderDelivery;
import com.example.model.OrderPayment;
import com.example.model.OrderProduct;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.OrderDeliveryRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
@Transactional(readOnly = true)
public class OrderService {

	@Autowired
	private OrderDeliveryRepository orderDeliveryRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Optional<Order> findOne(Long id) {
		return orderRepository.findById(id);
	}

	@Transactional(readOnly = false)
	public Order save(Order entity) {
		return orderRepository.save(entity);
	}

	@Transactional(readOnly = false)
	public Order create(OrderForm.Create entity) {
		Order order = new Order();
		order.setCustomerId(entity.getCustomerId());
		order.setShipping(entity.getShipping());
		order.setNote(entity.getNote());
		order.setPaymentMethod(entity.getPaymentMethod());
		order.setStatus(OrderStatus.ORDERED);
		order.setPaymentStatus(PaymentStatus.UNPAID);
		order.setPaid(0.0);

		var orderProducts = new ArrayList<OrderProduct>();
		entity.getOrderProducts().forEach(p -> {
			var product = productRepository.findById(p.getProductId()).get();
			var orderProduct = new OrderProduct();
			orderProduct.setProductId(product.getId());
			orderProduct.setCode(product.getCode());
			orderProduct.setName(product.getName());
			orderProduct.setQuantity(p.getQuantity());
			orderProduct.setPrice((double)product.getPrice());
			orderProduct.setDiscount(p.getDiscount());
			orderProduct.setTaxType(TaxType.get(product.getTaxType()));
			orderProducts.add(orderProduct);
		});

		// 計算
		var total = 0.0;
		var totalTax = 0.0;
		var totalDiscount = 0.0;
		for (var orderProduct : orderProducts) {
			var price = orderProduct.getPrice();
			var quantity = orderProduct.getQuantity();
			var discount = orderProduct.getDiscount();
			var tax = 0.0;
			/**
			 * 税額を計算する
			 */
			if (orderProduct.getTaxIncluded()) {
				// 税込みの場合
				tax = price * quantity * orderProduct.getTaxRate() / (100 + orderProduct.getTaxRate());
			} else {
				// 税抜きの場合
				tax = price * quantity * orderProduct.getTaxRate() / 100;
			}
			// 端数処理
			tax = switch (orderProduct.getTaxRounding()) {
			case TaxType.ROUND -> Math.round(tax);
			case TaxType.CEIL -> Math.ceil(tax);
			case TaxType.FLOOR -> Math.floor(tax);
			default -> tax;
			};
			var subTotal = price * quantity + tax - discount;
			total += subTotal;
			totalTax += tax;
			totalDiscount += discount;
		}
		order.setTotal(total);
		order.setTax(totalTax);
		order.setDiscount(totalDiscount);
		order.setGrandTotal(total + order.getShipping());
		order.setOrderProducts(orderProducts);

		orderRepository.save(order);

		return order;

	}

	@Transactional()
	public void delete(Order entity) {
		orderRepository.delete(entity);
	}

	@Transactional(readOnly = false)
	public void createPayment(OrderForm.CreatePayment entity) {
		var order = orderRepository.findById(entity.getOrderId()).get();
		/**
		 * 新しい支払い情報を登録する
		 */
		var payment = new OrderPayment();
		payment.setType(entity.getType());
		payment.setPaid(entity.getPaid());
		payment.setMethod(entity.getMethod());
		payment.setPaidAt(entity.getPaidAt());

		/**
		 * 支払い情報を更新する
		 */
		// orderのorderPaymentsに追加
		order.getOrderPayments().add(payment);
		// 支払い済み金額を計算
		var paid = order.getOrderPayments().stream().mapToDouble(p -> p.getPaid()).sum();
		// 合計金額から支払いステータスを判定
		var paymentStatus = paid > order.getGrandTotal() ? PaymentStatus.OVERPAID
				: paid < order.getGrandTotal() ? PaymentStatus.PARTIALLY_PAID : PaymentStatus.PAID;

		// 更新
		order.setPaid(paid);
		order.setPaymentStatus(paymentStatus);
		orderRepository.save(order);
	}

	/**
	 * CSVインポート処理
	 *
	 * @param file
	 * @throws IOException
	 */
	@Transactional
	public List<OrderDelivery> importCSV(MultipartFile file) throws IOException, ParseException {

		List<OrderDelivery> orderDeliveries = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // Skip header line

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Change the date format as needed

			while ((line = br.readLine()) != null) {
				final String[] split = line.replace("\"", "").split(",");
				if (split.length >= 5) {
					Integer orderId = Integer.parseInt(split[0]);
					String shippingCode = split[1];
					Date shippingDate = dateFormat.parse(split[2]);
					Date deliveryDate = dateFormat.parse(split[3]);
					String deliveryTimezone = split[4];
					boolean checked = false;
					String uploadStatus = "";
					OrderDelivery orderDelivery = new OrderDelivery(orderId, shippingCode, shippingDate, deliveryDate,
							deliveryTimezone, checked, uploadStatus);
					orderDeliveries.add(orderDelivery);
				}
			}

			// this is batch proccessing. not important now. It working, adding the data
			// from csv to db.But i need to add the data from the file into the page.
			// if (!orderDeliveries.isEmpty()) {
			// orderDeliveryRepository.saveAll(orderDeliveries);
			// }
		} catch (IOException | ParseException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}

		return orderDeliveries;
	}

	// Process CSV file (validation, parsing, and storing data)
	public void processCSV(MultipartFile file) {
		// Implement CSV processing logic here
	}

	// Update shipping information based on user selection
	public void updateShippingInfo(long orderId) {
		Optional<OrderDelivery> existingOrder = orderDeliveryRepository.findById(orderId);

		// If the order does not exist, create a new order with shipping information
		OrderDelivery newDelivery = new OrderDelivery();
		newDelivery.setOrderId((int)orderId);

	}

	@Transactional
	public String saveOrderDelivery(OrderDelivery orderDelivery) {
		// Check if an existing record with the same data exists
		Optional<Order> existingOrder = orderRepository.findById(
				orderDelivery.getOrderId());

		if (existingOrder.isPresent()) {
			var existingOrderStatus = existingOrder.get().getPaymentStatus();
			if (existingOrderStatus.equals(OrderStatus.COMPLETED)) {
				return "error";
			} else {
				try {
					orderDeliveryRepository.save(orderDelivery);
					if (!existingOrder.get().getStatus().equals(OrderStatus.SHIPPED)) {
						existingOrder.get().setStatus(OrderStatus.SHIPPED);
						orderRepository.save(existingOrder.get());
					}
					return "success";
				} catch (Exception e) {
					e.printStackTrace();
					return "error";
				}
			}
		} else {
			return "error";
		}

	}

}
