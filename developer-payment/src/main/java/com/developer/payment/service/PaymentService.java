package com.developer.payment.service;


import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;

import java.math.BigDecimal;

public interface PaymentService {

    DeveloperResult<Boolean> doPay(PaymentInfoDTO dto);

    DeveloperResult<BigDecimal> confirmReceipt(Long id);

    DeveloperResult<Boolean> amountRefunded(Long id);
}
