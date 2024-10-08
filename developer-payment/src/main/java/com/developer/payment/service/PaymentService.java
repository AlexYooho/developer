package com.developer.payment.service;


import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;

public interface PaymentService {

    DeveloperResult<Boolean> doPay(SendRedPacketsDTO dto);
}
