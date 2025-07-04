package com.developer.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendRedPacketsResultDTO {

    @JsonProperty("id")
    private String messageId;

}
