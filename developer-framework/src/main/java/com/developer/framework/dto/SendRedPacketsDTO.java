package com.developer.framework.dto;

import com.developer.framework.enums.RedPacketsTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class SendRedPacketsDTO implements Serializable {

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
     * 消息id
     */
    private Long messageId;
}
