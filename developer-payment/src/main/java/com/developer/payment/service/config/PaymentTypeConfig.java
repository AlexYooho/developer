package com.developer.payment.service.config;

import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.payment.service.payment.redpackets.RedPacketsPaymentService;
import com.developer.payment.service.payment.transfer.TransferMoneyPaymentService;
import com.developer.payment.service.register.PaymentTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class PaymentTypeConfig {

    @Bean
    public PaymentTypeRegister registerPaymentTypeInstance(
            TransferMoneyPaymentService transferMoneyPaymentService,
            RedPacketsPaymentService redPacketsPaymentService){
        PaymentTypeRegister typeRegister = new PaymentTypeRegister();
        typeRegister.registerPaymentTypeInstance(PaymentTypeEnum.TRANSFER, transferMoneyPaymentService);
        typeRegister.registerPaymentTypeInstance(PaymentTypeEnum.RED_PACKETS, redPacketsPaymentService);
        return typeRegister;
    }

}
