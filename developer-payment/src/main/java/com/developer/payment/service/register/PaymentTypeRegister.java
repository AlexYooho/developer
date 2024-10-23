package com.developer.payment.service.register;

import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

public class PaymentTypeRegister {

    private final Map<PaymentTypeEnum, PaymentService> map = new HashMap<>();

    public PaymentService findPaymentTypeInstance(PaymentTypeEnum typeEnum) {
        return map.get(typeEnum);
    }

    public void registerPaymentTypeInstance(PaymentTypeEnum typeEnum, PaymentService paymentService) {
        map.put(typeEnum, paymentService);
    }
}
