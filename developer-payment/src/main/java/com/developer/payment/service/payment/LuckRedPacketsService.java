package com.developer.payment.service.payment;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LuckRedPacketsService extends RedPacketsPaymentService{

    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets() {
        return null;
    }
}
