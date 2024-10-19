package com.developer.payment.service.payment.redpackets;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.PaymentInfoDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedPacketsPaymentService implements PaymentService {

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto){
        return redPacketsTypeRegister.findInstance(dto.getSendRedPacketsDTO().getType()).sendRedPackets(dto.getSendRedPacketsDTO(),dto.getChannel());
    }
}
