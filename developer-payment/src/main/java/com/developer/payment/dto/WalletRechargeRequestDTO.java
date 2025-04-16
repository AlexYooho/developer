package com.developer.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRechargeRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("verify_code")
    private Integer verifyCode;

    @JsonProperty("payment_password")
    private Integer paymentPassword;

}
