package com.developer.payment.dto;

import com.developer.framework.enums.RedPacketsChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.enums.PaymentTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendRedPacketsDTO {

    /**
     * 红包金额
     */
    private BigDecimal redPacketsAmount;

    /**
     * 目标id:群id，用户id
     */
    private Long targetId;

    /**
     * 红包个数
     */
    private Integer totalCount;

    /**
     * 支付类型
     */
    private PaymentTypeEnum paymentTypeEnum;

    /**
     * 红包类型
     */
    private RedPacketsTypeEnum type;

    /**
     * 红包渠道
     */
    private RedPacketsChannelEnum channel;

    /**
     * 消息id
     */
    private Long messageId;
}
