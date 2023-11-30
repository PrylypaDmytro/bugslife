package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.enums.FileImportStatus;
import com.example.enums.PaymentMethod;
import com.example.enums.ServiceType;
import com.example.model.FileImportInfo;
import com.example.model.Order;
import com.example.model.OrderPayment;
import com.example.repository.FileImportInfoRepository;
import com.example.repository.PaymentAmountRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PaymentAmountService {

	@Autowired
	private PaymentAmountRepository paymentAmountRepository;

	@Autowired
	private FileImportInfoRepository fileImportInfoRepository;

	public List<OrderPayment> findAll() {
		return paymentAmountRepository.findAll();
	}

	public Optional<OrderPayment> findOne(Long id) {
		return paymentAmountRepository.findById(id);
	}

	public List<OrderPayment> findByCompany(Order order) {
		return paymentAmountRepository.findByOrder(order);
	}

	@Transactional(readOnly = false)
	public OrderPayment save(OrderPayment entity) {
		return paymentAmountRepository.save(entity);
	}

	@Transactional(readOnly = false)
	public void delete(OrderPayment entity) {
		paymentAmountRepository.delete(entity);
	}

	/**
	 * 入力値のバリデーションチェックを行う
	 *
	 * @param entity
	 * @return boolean
	 */
	public boolean validate(OrderPayment entity) {
		// 収支が選択されているか
		// if (Objects.isNull(entity.getPlusMinus())) {
		// return false;
		// }
		// // 金額が入力されているか
		// if (Objects.isNull(entity.getPrice())) {
		// return false;
		// }
		// // 金額が0以上か
		// if (entity.getPrice() <= 0) {
		// return false;
		// }
		// // 金額が10億円以下か
		// if (entity.getPrice() >= Validate.PRICE_UPPER) {
		// return false;
		// }
		// // 支出の場合、金額が5億以下か
		// if (Objects.isNull(entity.getPlusMinus()) && entity.getPrice() >=
		// Validate.EXPENSE_PRICE_UPPER) {
		// return false;
		// }
		// // メモは1000文字以下か
		// if (Objects.nonNull(entity.getMemo())) {
		// if (entity.getMemo().length() >= Validate.TEXT_LENGTH) {
		// return false;
		// }
		// }

		return true;
	}

	/**
	 * TransactionalAmountの収支合計を取得する
	 *
	 * @param order
	 * @return Double
	 */
	public Double getSumTransactionalAmounts(Order order) {
		List<OrderPayment> opAmountList = this.findByCompany(order);

		// 収入の場合は加算、支出の場合は減算
		Double sum = 0.0;
		// for (OrderPayment opAmount : opAmountList) {
		// if (opAmount.getPlusMinus()) {
		// sum += opAmount.getPrice();
		// } else {
		// sum -= opAmount.getPrice();
		// }
		// }

		// // 表示はXXX千円とするので、1000で割って余りは切り捨てて丸める
		// // sum = Math.round(sum / (double)1000);
		// sum = sum / 1000;

		return sum;
	}

	/**
	 * 収支比率を計算する
	 */
	public Double getRatioTransactionalAmounts(Order order) {
		// 現在の取得を行う
		List<OrderPayment> opAmountList = this.findByCompany(order);

		// 収入と支出の比率を計算する
		Double incomSum = 0.0;
		Double expenseSum = 0.0;
		// for (OrderPayment opAmount : opAmountList) {
		// if (opAmount.getPlusMinus()) {
		// incomSum += opAmount.getPrice();
		// } else {
		// expenseSum += opAmount.getPrice();
		// }
		// }
		double ratio = (double)incomSum / (expenseSum + incomSum);
		return ratio * 100;
	}

	/**
	 * 非同期で取引金額のCSVファイルを取り込む
	 *
	 * @param file
	 * @param orderId
	 * @throws Exception
	 * @return void
	 * @todo 非同期化
	 */
	@Transactional
	public void importCSV(MultipartFile file, Long orderId) throws Exception {
		// アップデート後のインスタンス
		FileImportInfo updatedImp;
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");

		// CSV取込親テーブルに取込中でデータを登録する
		try {
			// CSV取込親テーブルのインスタンスを生成
			FileImportInfo imp = new FileImportInfo();
			// CSV取込親テーブルのインスタンスにデータをセットする
			imp.setStartDatetime(LocalDateTime.now());
			imp.setStatus(FileImportStatus.IMPORTING);
			imp.setRelationId(orderId);
			imp.setType(ServiceType.ORDER);
			// CSV取込親テーブルにデータを登録する
			updatedImp = fileImportInfoRepository.save(imp);
		} catch (Exception e) {
			// エラーはコントローラーで処理
			e.printStackTrace();
			throw e;
		}

		// CSVファイルの読み込み
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int lineCouter = 0;
			while ((line = br.readLine()) != null) {
				// 行数カウントアップ
				lineCouter++;

				// 1行目はヘッダーなので読み飛ばす
				if (lineCouter == 1) {
					continue;
				}

				// csvなのでカンマ区切りで分割する
				final String[] split = line.replace("\"", "").split(",");

				// 取引金額のインスタンスを生成
				OrderPayment orderPayment = new OrderPayment();
				orderPayment.setOrderId(orderId);

				// 取引金額のインスタンスにCSVファイルから読み取ったデータをセットする
				orderPayment.setPaid(Double.parseDouble(split[0]));
				orderPayment.setMethod(split[1]);
				Date parseDate = sdFormat.parse(split[2]);
				orderPayment.setPaidAt(new Timestamp(parseDate.getTime()));

				if (orderPayment.getMethod().equals(PaymentMethod.CREDIT_CARD)) {
					orderPayment.setType("Authorization");
				} else if (orderPayment.getMethod().equals(PaymentMethod.PAYMENT_ON_DELIVERY)) {
					orderPayment.setType("Instant");
				}
				// orderPayment.setType(line);
				// OrderPayment.setHasPaid(Boolean.parseBoolean(split[3]));
				// OrderPayment.setMemo(split[4]);

				// 取引金額を保存する
				paymentAmountRepository.save(orderPayment);
			}
			updatedImp.setStatus(FileImportStatus.COMPLETE);
		} catch (Exception e) {
			// 失敗の場合、ステータスをエラーにする
			updatedImp.setStatus(FileImportStatus.ERROR);
			// エラーはコントローラーで処理
			e.printStackTrace();
			throw e;
		} finally {
			// 取込完了日時をセットして処理終了
			updatedImp.setEndDatetime(LocalDateTime.now());
			fileImportInfoRepository.save(updatedImp);
		}
	}

	public List<OrderPayment> findByOrder(Order order) {
		return paymentAmountRepository.findByOrder(order);
	}
}
