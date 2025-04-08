package com.developer.payment.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ReturnTransferRequestDTO {
    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("red_packets_id")
    private Long redPacketsId;
}
