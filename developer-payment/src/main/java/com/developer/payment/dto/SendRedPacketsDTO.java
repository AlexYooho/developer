package com.developer.payment.dto;

import com.developer.payment.enums.RedPacketsChannelEnum;
import com.developer.payment.enums.RedPacketsTypeEnum;
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
     * 红包类型
     */
    private RedPacketsTypeEnum type;

    /**
     * 红包渠道
     */
    private RedPacketsChannelEnum channel;
}
