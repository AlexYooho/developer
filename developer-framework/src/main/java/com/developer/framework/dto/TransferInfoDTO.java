package com.developer.framework.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class TransferInfoDTO implements Serializable {

    /**
     * 转账金额
     */
    private BigDecimal transferAmount;

    /**
     * 转账目标用户id
     */
    private Long targetId;

}
