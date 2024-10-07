package com.developer.payment.service.redpackets;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;

import java.math.BigDecimal;
import java.util.List;

public interface RedPacketsService {

    DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto);

    DeveloperResult<List<BigDecimal>> distributeRedPacketsAmount(BigDecimal totalAmount, int totalCount);

}
