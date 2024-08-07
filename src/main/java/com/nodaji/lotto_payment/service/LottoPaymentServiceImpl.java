package com.nodaji.lotto_payment.service;

import com.nodaji.lotto_payment.config.apis.ApiPoint;
import com.nodaji.lotto_payment.domain.dao.LottoPaymentDao;
import com.nodaji.lotto_payment.domain.dto.request.KafkaPayInfoRequest;
import com.nodaji.lotto_payment.domain.dto.request.LottoPayRequest;
import com.nodaji.lotto_payment.domain.dto.request.LottoPaymentRequest;
import com.nodaji.lotto_payment.domain.dto.response.LottoPaymentResponse;
import com.nodaji.lotto_payment.domain.dto.response.PointResponse;
import com.nodaji.lotto_payment.domain.entity.LottoPayment;
import com.nodaji.lotto_payment.domain.entity.TotalPoint;
import com.nodaji.lotto_payment.domain.repository.LottoPaymentRepository;
import com.nodaji.lotto_payment.domain.repository.TotalPointRepository;
import com.nodaji.lotto_payment.kafka.producer.KafkaProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class LottoPaymentServiceImpl implements LottoPaymentService{
    private final LottoPaymentRepository lottoPaymentRepository;
    private final LottoPaymentDao lottoPaymentDao;
    private final TotalPointRepository totalPointRepository;
    private final ApiPoint apiPoint;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public void save(String userId, List<LottoPaymentRequest> requests) {
        if (requests.isEmpty()) throw new IllegalArgumentException("Not Buy");

//        point 전달 유효한지 체크
        PointResponse pointResponse = apiPoint.getPoint(userId);
        if (pointResponse.amount() < (requests.size() * 1000L)) throw new IllegalArgumentException("포인트가 부족합니다.");

//        payment에 point 전달
        apiPoint.subtractPoint(userId, LottoPayRequest.payRequest("동행복권", requests.size()*1000L));


        //기존 회차가 등록되어있다면 더해서 덮어쓰고, 등록되어있지않으면 새로 생성하는 로직
        Long finalRound = lottoPaymentDao.getRound();
        if(totalPointRepository.findByRound(finalRound) == null) {
            totalPointRepository.save(new TotalPoint(finalRound, requests.size()*1000L));
        }
        else{
            TotalPoint byRound = totalPointRepository.findByRound(finalRound);
            byRound.setTotalPoint(byRound.getTotalPoint()+requests.size()*1000L);
        }

        requests.forEach(req -> {
            try {
//                System.out.println("tdtdstsatas" + req);
                LottoPayment lottoPayment = lottoPaymentRepository.save(req.toEntity(finalRound, userId));

                // id, userid, date, round 구매 내역 전달
                KafkaPayInfoRequest kafkaPayInfoRequest = new KafkaPayInfoRequest(lottoPayment.getId(), lottoPayment.getUserId(), lottoPayment.getCreateAt(), lottoPayment.getRound());
                kafkaProducer.sendPay(kafkaPayInfoRequest, "history-topic");
                apiPoint.createResult(kafkaPayInfoRequest);
            } catch (Exception e) {
                System.err.println("Error saving LottoPayment: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<LottoPaymentResponse> getAllByUserIdAndRoundId(Long round) {
        // 유저 id 필요
        return lottoPaymentRepository.findByRound(round);
    }

}
