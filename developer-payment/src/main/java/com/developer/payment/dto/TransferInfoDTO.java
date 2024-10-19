package com.developer.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
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
