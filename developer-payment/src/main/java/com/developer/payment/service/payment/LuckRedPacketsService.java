package com.developer.payment.service.payment;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.service.RedPacketsService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class LuckRedPacketsService extends BaseRedPacketsService implements RedPacketsService {

    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets() {
        return null;
    }

    @Override
    public List<BigDecimal> distributeRedPacketsAmount(BigDecimal totalAmount, Integer totalCount) {
        return null;
    }
}
