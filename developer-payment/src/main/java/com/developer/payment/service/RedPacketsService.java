package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;

import java.math.BigDecimal;

public interface RedPacketsService {

    DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto);

    DeveloperResult<BigDecimal> openRedPackets(Long redPacketsId);
}
