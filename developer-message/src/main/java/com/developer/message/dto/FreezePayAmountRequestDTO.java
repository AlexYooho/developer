package com.developer.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FreezePayAmountRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("amount")
    private BigDecimal amount;

}