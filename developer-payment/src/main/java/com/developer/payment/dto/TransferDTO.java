package com.developer.payment.dto;

import com.developer.framework.enums.payment.PaymentChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferDTO {

    /**
     * 转账总金额
     */
    private BigDecimal amount;

    /**
     * 转账渠道类型
     */
    private PaymentChannelEnum paymentChannel;

    /**
     * 转账对象id
     */
    private Long toUserId;

    /**
     * 转账对象群组id
     */
    private Long toGroupId;
}
