package com.developer.payment.service;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.SendRedPacketsDTO;

import java.math.BigDecimal;

public interface RedPacketsService {

    DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto, PaymentChannelEnum paymentChannel);

    DeveloperResult<BigDecimal> openRedPackets(Long redPacketsId);
}
