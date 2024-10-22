package com.developer.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferInfoDTO {

    /**
     * 转账金额
     */
    private BigDecimal transferAmount;

    /**
     * 转账目标用户id
     */
    private Long targetId;

}
