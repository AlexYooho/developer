package com.developer.framework.dto;

import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class TransferInfoDTO implements Serializable {

    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 转账金额
     */
    private BigDecimal transferAmount;

    /**
     * 转账对象
     */
    @JsonProperty("target_id")
    private Long targetId;

    /**
     * 支付渠道
     */
    private PaymentChannelEnum paymentChannel;
}
