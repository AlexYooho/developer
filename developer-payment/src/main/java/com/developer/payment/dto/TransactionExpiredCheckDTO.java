package com.developer.payment.dto;

import com.developer.framework.enums.PaymentTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionExpiredCheckDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("payment_type")
    private PaymentTypeEnum paymentTypeEnum;

    @JsonProperty("transaction_id")
    private Long transactionId;
}
