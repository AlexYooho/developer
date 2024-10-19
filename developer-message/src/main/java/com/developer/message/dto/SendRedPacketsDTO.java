package com.developer.message.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
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
     * 红包类型
     */
    private RedPacketsTypeEnum type;

    /**
     * 支付渠道
     */
    private PaymentChannelEnum channel;

    /**
     * 消息id
     */
    private Long messageId;

}
