package com.developer.payment.service;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.SendRedPacketsDTO;

import java.math.BigDecimal;

public interface RedPacketsService {

    RedPacketsTypeEnum redPacketsType();

    DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto);

    DeveloperResult<BigDecimal> openRedPackets(String serialNo,Long redPacketsId);
}
