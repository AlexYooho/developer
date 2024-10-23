package com.developer.payment.service;


import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;

public interface PaymentService {

    DeveloperResult<Boolean> doPay(PaymentInfoDTO dto);
}
