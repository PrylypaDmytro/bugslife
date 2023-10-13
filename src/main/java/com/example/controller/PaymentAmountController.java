package com.example.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
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
import com.example.model.Company;
import com.example.model.OrderPayment;
import com.example.service.OrderService;
import com.example.service.PaymentAmountService;

import com.example.model.Order;
import com.example.model.OrderPayment; // paymentAmount (TransanctionAmount)
import com.example.service.OrderService;
import com.example.service.PaymentAmountService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/paymentAmounts")
public class PaymentAmountController {

	@Autowired
	private PaymentAmountService paymentAmountService;
	@Autowired
	private OrderService orderService;

	/**
	 * 取引金額情報の詳細画面表示
	 *
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String show(Model model, @PathVariable("id") Long id) {
		if (id != null) {
			Optional<OrderPayment> opAmountOpt = paymentAmountService.findOne(id);
			model.addAttribute("opAmount", opAmountOpt.get());
		}
		return "payment_amount/show";
	}

	/**
	 * 取引金額情報の新規作成処理
	 *
	 * @param model
	 * @param entity
	 * @return
	 */
	@GetMapping(value = "/{o_id}/new")
	public String create(Model model, @ModelAttribute OrderPayment entity, @PathVariable("o_id") Long order_id) {
		Optional<Order> orderOpt = orderService.findOne(order_id);

		// 取引先情報が存在する場合、取引金額情報に取引先情報をセットする
		if (orderOpt.isEmpty()) {
			// 取引先情報の連係ミスが生じた場合は、エラーを返す
			throw new ServiceException(Message.MSG_ERROR);
		}

		// entity.setOrderId(orderOpt.get().getId());
		entity.setOrder(orderOpt.get());
		model.addAttribute("opAmount", entity);
		return "payment_amount/form";
	}

	/**
	 * 取引金額情報のUPSERT処理
	 *
	 * @param entity
	 * @param result
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping
	public String create(@ModelAttribute OrderPayment entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		OrderPayment opAmount = null;
		try {
			// 入力内容のバリデーションチェック
			if (!paymentAmountService.validate(entity)) {
				// NG
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/orders";
			}

			opAmount = paymentAmountService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_INSERT);
			return "redirect:/orders/" + opAmount.getOrderId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	/**
	 * 取引金額情報の編集画面表示
	 *
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}/edit")
	public String update(Model model, @PathVariable("id") Long id) {
		try {
			if (id != null) {
				Optional<OrderPayment> opAmountOpt = paymentAmountService.findOne(id);
				model.addAttribute("opAmount", opAmountOpt.get());
			}
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return "payment_amount/form";
	}

	/**
	 * 取引金額情報の更新処理
	 *
	 * @param entity
	 * @param result
	 * @param redirectAttributes
	 * @return
	 */
	@PutMapping
	public String update(@Validated @ModelAttribute OrderPayment entity, BindingResult result,
			RedirectAttributes redirectAttributes) {
		OrderPayment opAmount = null;
		try {
			// 入力内容のバリデーションチェック
			if (!paymentAmountService.validate(entity)) {
				// NG
				redirectAttributes.addFlashAttribute("error", Message.MSG_VALIDATE_ERROR);
				return "redirect:/orders";
			}

			opAmount = paymentAmountService.save(entity);
			redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_UPDATE);
			return "redirect:/orders/" + opAmount.getOrderId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			e.printStackTrace();
			return "redirect:/orders";
		}
	}

	/**
	 * 取引金額情報の削除処理
	 *
	 * @param id                 取引先ID
	 * @param redirectAttributes リダイレクト先に値を渡す
	 * @return
	 */
	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			if (id != null) {
				Optional<OrderPayment> opAmountOpt = paymentAmountService.findOne(id);
				paymentAmountService.delete(opAmountOpt.get());
				redirectAttributes.addFlashAttribute("success", Message.MSG_SUCESS_DELETE);
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", Message.MSG_ERROR);
			throw new ServiceException(e.getMessage());
		}
		return "redirect:/orders";
	}

	/**
	 * 取引金額CSVインポート処理
	 *
	 * @param csvFile
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/{o_id}/upload_csv")
	public String uploadCSVFile(@PathVariable("o_id") Long orderId, @RequestParam("csv_file") MultipartFile csvFile,
			RedirectAttributes redirectAttributes) {

		String redirectUrl = "redirect:/orders/" + orderId;
		if (csvFile.isEmpty()) {
			// ファイルが存在しない場合
			redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
			return redirectUrl;
		}
		if (!"text/csv".equals(csvFile.getContentType())) {
			// CSVファイル以外の場合
			redirectAttributes.addFlashAttribute("error", "CSVファイルを選択してください。");
			return redirectUrl;
		}

		// csvファイルのインポート処理
		try {
			paymentAmountService.importCSV(csvFile, orderId);
		} catch (Throwable t) {
			redirectAttributes.addFlashAttribute("error", t.getMessage());
			t.printStackTrace();
			return redirectUrl;
		}

		return redirectUrl;
	}

	/**
	 * CSVテンプレートダウンロード処理
	 *
	 * @param response
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/download")
	public String download(HttpServletResponse response, RedirectAttributes redirectAttributes) {

		try (OutputStream os = response.getOutputStream();) {
			Path filePath = new ClassPathResource("static/templates/order_payment_amounts.csv").getFile().toPath();
			byte[] fb1 = Files.readAllBytes(filePath);
			String attachment = "attachment; filename=payment_amounts_" + new Date().getTime() + ".csv";

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", attachment);
			response.setContentLength(fb1.length);
			os.write(fb1);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
