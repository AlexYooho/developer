package com.developer.payment.service.payment;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.payment.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public abstract class RedPacketsPaymentService implements PaymentService {

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    @Transactional
    public DeveloperResult<Boolean> doPay(SendRedPacketsDTO dto){
        return redPacketsTypeRegister.findRedPacketsTypeInstance(dto.getType()).sendRedPackets(dto);
    }

    public abstract DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto);

    public abstract DeveloperResult<BigDecimal> openRedPackets();
}
