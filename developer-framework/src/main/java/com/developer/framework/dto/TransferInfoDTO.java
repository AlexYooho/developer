package com.developer.framework.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

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
     * 转账对象id
     */
    private Long toUserId;

    /**
     * 转账对象群组id
     */
    private Long toGroupId;

    /**
     * 支付渠道
     */
    private PaymentChannelEnum paymentChannel;
}
