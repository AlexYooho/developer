package com.developer.payment.service.config;

import com.developer.payment.enums.PaymentTypeEnum;
import com.developer.payment.service.payment.redpackets.RedPacketsPaymentService;
import com.developer.payment.service.payment.transfer.TransferMoneyPaymentService;
import com.developer.payment.service.register.PaymentTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTypeConfig {

    @Autowired
    private TransferMoneyPaymentService transferMoneyPaymentService;

    @Autowired
    private RedPacketsPaymentService redPacketsPaymentService;

    public PaymentTypeRegister register(){
        PaymentTypeRegister typeRegister = new PaymentTypeRegister();
        typeRegister.registerPaymentTypeInstance(PaymentTypeEnum.TRANSFER, transferMoneyPaymentService);
        typeRegister.registerPaymentTypeInstance(PaymentTypeEnum.RED_PACKETS, redPacketsPaymentService);
        return typeRegister;
    }

}
