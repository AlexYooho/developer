package com.developer.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModifyRedPacketsMessageStatusRequestDTO {
    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("message_status")
    private Integer messageStatus;
}
