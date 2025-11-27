package com.developer.rpc.dto.payment.request;

import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.enums.payment.PaymentTypeEnum;
import com.developer.framework.enums.payment.RedPacketsTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InvokeRedPacketsTransferRequestRpcDTO implements Serializable {


    /*
    支付类型
     */
    @JsonProperty("payment_type")
    private PaymentTypeEnum paymentType;

    /**
     * 红包金额
     */
    @JsonProperty("payment_amount")
    private BigDecimal paymentAmount;

    /**
     * 目标id:群id，用户id
     */
    @JsonProperty("target_id")
    private Long targetId;

    /**
     * 红包个数
     */
    @JsonProperty("red_packets_total_count")
    private Integer redPacketsTotalCount;

    /**
     * 红包类型
     */
    @JsonProperty("red_packets_type")
    private RedPacketsTypeEnum redPacketsType;

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
