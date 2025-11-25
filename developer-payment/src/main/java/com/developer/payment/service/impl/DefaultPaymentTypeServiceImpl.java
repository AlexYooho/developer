package com.developer.payment.service.impl;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.enums.payment.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultPaymentTypeServiceImpl implements PaymentService {
    @Override
    public PaymentTypeEnum paymentType() {
        return null;
    }

    @Override
    public DeveloperResult<SendRedPacketsResultDTO> doPay(PaymentInfoDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<BigDecimal> amountCharged(OpenRedPacketsRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> amountRefunded(ReturnTransferRequestDTO req) {
        return null;
    }
}
