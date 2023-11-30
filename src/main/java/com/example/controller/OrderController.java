package com.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.constants.Message;
import com.example.dto.ShippingListDto;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentMethod;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderShippingForm;
import com.example.model.Order;
import com.example.model.OrderDelivery;
import com.example.model.OrderPayment;
import com.example.service.PaymentAmountService;
import com.example.service.OrderService;
import com.example.service.ProductService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private ShippingListDto orderShippingData;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private PaymentAmountService paymentAmountService;

	@ModelAttribute
	ShippingListDto setFormDto() {
		return new ShippingListDto();
	}

	@GetMapping
	public String index(Model model) {
		List<Order> all = orderService.findAll();
		model.addAttribute("listOrder", all);
		return "order/index";
	}

	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if (id != null) {
			Optional<Order> order = orderService.findOne(id);
			model.addAttribute("order", order.get());

			List<OrderPayment> listOpAmounts = paymentAmountService.findByOrder(order.get());
			model.addAttribute("listOpAmount", listOpAmounts);
		}

		return "order/show";
	}

	@GetMapping(value = "/new")
	public String create(Model model, @ModelAttribute OrderForm.Create entity) {
		model.addAttribute("order", entity);
		model.addAttribute("products", productService.findAll());
		model.addAttribute("paymentMethods", PaymentMethod.values());
		return "order/create";
	}

	@PostMapping
	public String create(@Validated @ModelAttribute OrderForm.Create entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.create(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@GetMapping("/{id}/edit")
	public String update(Model model, @PathVariable("id") Long id) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				model.addAttribute("order", entity.get());
				model.addAttribute("paymentMethods", PaymentMethod.values());
				model.addAttribute("paymentStatus", PaymentStatus.values());
				model.addAttribute("orderStatus", OrderStatus.values());
			}
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return "order/form";
	}

	@PutMapping
	public String update(@Validated @ModelAttribute Order entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		Order order = null;
		try {
			order = orderService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_UPDATE);
			return "redirect:/orders/" + order.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			if (id != null) {
				Optional<Order> entity = orderService.findOne(id);
				orderService.delete(entity.get());
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_DELETE);
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			throw new ServiceException(e.getMessage());
		}
		return "redirect:/orders";
	}

	@PostMapping("/{id}/payments")
	public String createPayment(@Validated @ModelAttribute OrderForm.CreatePayment entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		try {
			orderService.createPayment(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_PAYMENT_INSERT);
			return "redirect:/orders/" + entity.getOrderId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	// ===================== Shipping =====================

	// 未発送データを取得
	@PostMapping("/shipping/getUnshippedData")
	public String getUnshippedData(RedirectAttributes redirectAttributes, Model model) {
		try {
			List<OrderDelivery> orderDeliveries = orderService.getUnshippedData();

			if (!orderDeliveries.isEmpty()) {

				orderShippingData.setOrderShippingList(orderDeliveries);
				// Add the list to the model to display on the HTML page
				model.addAttribute("orderShippingData", orderShippingData);
			}
		} catch (Throwable e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			e.printStackTrace();
			return "redirect:/orders/shipping";
		}

		return "order/shipping";
	}

	// 発送一覧を表示する
	@GetMapping("/shipping")
	public String shipping(Model model) {
		// List<Order> all = orderService.findAll();
		// model.addAttribute("listOrder", all);
		return "order/shipping";
	}

	// CSVファイルをアップロードする
	@PostMapping("/shipping")
	public String uploadFile(@RequestParam("file") MultipartFile uploadFile, RedirectAttributes redirectAttributes,
			Model model) {

		if (uploadFile.isEmpty()) {
			// ファイルが存在しない場合
			redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
			return "redirect:/orders/shipping";
		}
		if (!"text/csv".equals(uploadFile.getContentType())) {
			// CSVファイル以外の場合
			redirectAttributes.addFlashAttribute("error", "CSVファイルを選択してください。");
			return "redirect:/orders/shipping";
		}
		try {
			List<OrderDelivery> orderDeliveries = orderService.importCSV(uploadFile);
			// List<OrderDelivery> orderService.importCSV(uploadFile);
			if (!orderDeliveries.isEmpty()) {

				orderShippingData.setOrderShippingList(orderDeliveries);
				// Add the list to the model to display on the HTML page
				model.addAttribute("orderShippingData", orderShippingData);
			} else {
				redirectAttributes.addFlashAttribute("error", "データがありません。");
				return "redirect:/orders/shipping";
			}
		} catch (Throwable e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			e.printStackTrace();
			return "redirect:/orders/shipping";
		}

		return "order/shipping";
	}

	// データを更新する
	@PutMapping("/shipping")
	public String updateShippingInfo(@ModelAttribute("orderShippingData") OrderShippingForm orderShippingForm,
			RedirectAttributes redirectAttributes, Model model) {

		// Get the list of orderShippingData
		List<OrderDelivery> orderShippingList = orderShippingForm.getOrderShippingList();
		if (orderShippingList.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "データがありません。");
			return "redirect:/orders/shipping";
		}
		// Iterate through the checkedList and orderShippingList simultaneously
		for (int i = 0; i < orderShippingList.size(); i++) {
			if (orderShippingList.get(i).isChecked()) {
				// Checkbox is checked for this row, save the data to the database
				OrderDelivery orderDelivery = orderShippingList.get(i);
				String result = orderService.saveOrderDelivery(orderDelivery);
				// save the result of the update to the uploadStatus field
				orderShippingForm.getOrderShippingList().get(i).setUploadStatus(result);
			}
		}

		model.addAttribute("orderShippingData", orderShippingForm);
		model.addAttribute("validationError", "Shipping information updatedsuccessfully");
		return "order/shipping";
	}

	// テンプレートのCSVファイルをダウンロードする
	@PostMapping("/shipping/download")
	public void downloadTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try (OutputStream os = response.getOutputStream();) {
			Path filePath = new ClassPathResource("static/templates/order_shipping_template.csv").getFile().toPath();
			byte[] fb1 = Files.readAllBytes(filePath);
			String attachment = "attachment; filename=order_shipping_template_" + new Date().getTime() + ".csv";

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", attachment);
			response.setContentLength(fb1.length);
			os.write(fb1);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostMapping("/shipping/convertToCsv")
	public void convertToCsv(HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try (OutputStream os = response.getOutputStream()) {
			List<OrderDelivery> unshippedOrders = orderService.getUnshippedData();
			// Prepare a StringBuilder to construct CSV content
			StringBuilder csvContent = new StringBuilder();

			// Append CSV headers
			csvContent.append("Order ID,Item,Quantity\n"); // Example headers

			// Append data rows for each unshipped order
			for (OrderDelivery orderDelivery : unshippedOrders) {
				// Extracting values from OrderDelivery object
				long orderId = orderDelivery.getOrder().getId();
				String shippingCode = orderDelivery.getShippingCode();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String shippingDate = sdf.format(orderDelivery.getShippingDate());
				String deliveryDate = sdf.format(orderDelivery.getDeliveryDate());
				String deliveryTimezone = orderDelivery.getDeliveryTimezone();

				// Format data and append to the CSV content
				String orderRow = String.format("%d,%s,%s,%s,%s\n", orderId, shippingCode, shippingDate, deliveryDate,
						deliveryTimezone);
				csvContent.append(orderRow);
			}

			// Setting up response headers for file download
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=unshipped_orders.csv");

			// Write the CSV content to the response output stream
			os.write(csvContent.toString().getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
