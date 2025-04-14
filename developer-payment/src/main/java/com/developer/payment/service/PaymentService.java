package com.developer.payment.service;


import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentTypeEnum paymentType();

    DeveloperResult<Boolean> doPay(PaymentInfoDTO dto);

    DeveloperResult<BigDecimal> amountCharged(OpenRedPacketsRequestDTO req);

    DeveloperResult<Boolean> amountRefunded(ReturnTransferRequestDTO req);
}
