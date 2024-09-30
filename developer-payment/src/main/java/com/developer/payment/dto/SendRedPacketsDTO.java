package com.developer.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendRedPacketsDTO {

    private BigDecimal redPacketsAmount;

    private Long targetUserId;

}
