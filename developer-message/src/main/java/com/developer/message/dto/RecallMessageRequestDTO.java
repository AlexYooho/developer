package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RecallMessageRequestDTO {
    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("message_id")
    private Long messageId;
}
