package com.developer.payment.service.processorFactory;

import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.impl.DefaultPaymentTypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentTypeProcessorDispatchFactory {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DefaultPaymentTypeServiceImpl defaultPaymentTypeService;

    public PaymentService getInstance(PaymentTypeEnum paymentType){
        Map<String, PaymentService> beansMap = context.getBeansOfType(PaymentService.class);
        PaymentService instance = null;
        for (PaymentService item : beansMap.values()) {
            if (item.paymentType() != paymentType) {
                continue;
            }

            instance = item;
            break;
        }

        return instance == null ? defaultPaymentTypeService : instance;
    }
}
