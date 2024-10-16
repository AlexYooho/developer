package com.developer.payment.service.payment;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedPacketsPaymentService implements PaymentService {

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    public DeveloperResult<Boolean> doPay(SendRedPacketsDTO dto){
        return redPacketsTypeRegister.findInstance(dto.getType()).sendRedPackets(dto);
    }
}
