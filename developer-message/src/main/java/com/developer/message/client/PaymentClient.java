package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

@FeignClient(name="developer-gateway",contextId = "developer-payment",configuration = {FeignRequestInterceptor.class})
public interface PaymentClient {

    /**
     * 冻结支付金额
     * @param userId
     * @param amount
     * @return
     */
    @PostMapping("freeze-pay-amount")
    DeveloperResult<Boolean> freezePaymentAmount(Long userId, BigDecimal amount);

}
