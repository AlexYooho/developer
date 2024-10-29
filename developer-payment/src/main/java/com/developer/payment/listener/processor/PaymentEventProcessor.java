package com.developer.payment.listener.processor;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.register.PaymentTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProcessor implements IMessageProcessor {
    @Override
    public RabbitMQEventTypeEnum eventType() {
        return RabbitMQEventTypeEnum.PAYMENT;
    }

    @Autowired
    private PaymentTypeRegister paymentTypeRegister;

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        PaymentInfoDTO paymentInfo = dto.parseData(PaymentInfoDTO.class);
        PaymentService paymentTypeInstance = paymentTypeRegister.findPaymentTypeInstance(paymentInfo.getPaymentTypeEnum());
        return paymentTypeInstance.doPay(paymentInfo);
    }
}
