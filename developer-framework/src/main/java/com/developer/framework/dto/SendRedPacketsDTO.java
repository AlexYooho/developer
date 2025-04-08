package com.developer.framework.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendRedPacketsDTO {

    @JsonProperty("serial_no")
    private String serialNo;

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

    /**
     * 支付渠道
     */
    private PaymentChannelEnum paymentChannel;
}
