package com.nodaji.lotto_payment.domain.dto.request;

import com.nodaji.lotto_payment.domain.entity.LottoPayment;

import java.util.List;
import java.util.UUID;

public record LottoPaymentRequest(
        Integer first,
        Integer second,
        Integer third,
        Integer fourth,
        Integer fifth,
        Integer sixth
) {
    public LottoPayment toEntity(Long round) {
        return LottoPayment.builder()
                .first(first)
                .second(second)
                .third(third)
                .fourth(fourth)
                .fifth(fifth)
                .sixth(sixth)
                .round(round)
//                .userId(userId)
                .build();
    }


}
