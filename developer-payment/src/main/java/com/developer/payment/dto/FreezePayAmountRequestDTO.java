package com.developer.payment.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
public class FreezePayAmountRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("amount")
    private BigDecimal amount;

}
