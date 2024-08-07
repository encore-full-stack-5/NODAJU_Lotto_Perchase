package com.nodaji.lotto_payment.config.apis;

import com.nodaji.lotto_payment.domain.dto.request.KafkaPayInfoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "accounts-service11", url = "localhost:8080")
public interface FeignPoint1 {

    @PostMapping("/api/v1/lotto-history")
    void createResult(@RequestBody KafkaPayInfoRequest request);
}
