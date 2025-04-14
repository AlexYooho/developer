package com.developer.payment.service.impl.redpackets;

import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.service.RedPacketsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultRedPacketsTypeServiceImpl extends BaseRedPacketsService implements RedPacketsService {
    @Override
    public RedPacketsTypeEnum redPacketsType() {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets(String serialNo, Long redPacketsId) {
        return null;
    }
}
