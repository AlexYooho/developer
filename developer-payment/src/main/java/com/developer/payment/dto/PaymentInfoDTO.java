package com.developer.payment.dto;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.payment.enums.PaymentTypeEnum;
import lombok.Data;

@Data
public class PaymentInfoDTO {

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
