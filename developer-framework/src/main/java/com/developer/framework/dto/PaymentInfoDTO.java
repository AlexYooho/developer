package com.developer.framework.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.PaymentTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PaymentInfoDTO implements Serializable {

    /**
     * 红包信息
     */
    private SendRedPacketsDTO sendRedPacketsDTO;

    /**
     * 转账信息
     */
    private TransferInfoDTO transferInfoDTO;

    /**
     * 支付类型
     */
    private PaymentTypeEnum paymentTypeEnum;

    /**
     * 支付渠道
     */
    private PaymentChannelEnum channel;
}
