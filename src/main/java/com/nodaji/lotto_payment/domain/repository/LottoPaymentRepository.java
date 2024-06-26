package com.nodaji.lotto_payment.domain.repository;

import com.nodaji.lotto_payment.domain.dto.response.LottoPaymentResponse;
import com.nodaji.lotto_payment.domain.entity.LottoPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LottoPaymentRepository extends JpaRepository<LottoPayment, Long> {

    List<LottoPaymentResponse> findByRound(Long round);
}
