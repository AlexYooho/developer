package com.developer.payment.listener.processor;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.processorFactory.PaymentTypeProcessorDispatchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessor implements IMessageProcessor {
    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.PAYMENT;
    }

    @Autowired
    private PaymentTypeProcessorDispatchFactory dispatchFactory;

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        PaymentInfoDTO paymentInfo = dto.parseData(PaymentInfoDTO.class);
        PaymentService paymentTypeInstance = dispatchFactory.getInstance(paymentInfo.getPaymentTypeEnum());
        DeveloperResult<SendRedPacketsResultDTO> result = paymentTypeInstance.doPay(paymentInfo);
        return DeveloperResult.success(result.getSerialNo());
    }
}
