package com.developer.payment.service.impl.redpackets;

import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.impl.BasePaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultRedPacketsTypeServiceImpl extends BasePaymentService implements RedPacketsService {
    @Override
    public RedPacketsTypeEnum redPacketsType() {
        return null;
    }

    @Override
    public DeveloperResult<SendRedPacketsResultDTO> sendRedPackets(SendRedPacketsDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets(String serialNo, Long redPacketsId) {
        return null;
    }
}
