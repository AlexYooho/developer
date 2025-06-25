package com.developer.framework.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendRedPacketsDTO {

    /**
     * 红包金额
     */
    @JsonProperty("red_packets_amount")
    private BigDecimal redPacketsAmount;

    /**
     * 目标id:群id，用户id
     */
    @JsonProperty("target_id")
    private Long targetId;

    /**
     * 红包个数
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * 红包类型
     */
    @JsonProperty("type")
    private RedPacketsTypeEnum type;

    /**
     * 消息id
     */
    @JsonProperty("message_id")
    private Long messageId;

    /**
     * 支付渠道
     */
    @JsonProperty("payment_channel")
    private PaymentChannelEnum paymentChannel;
}
