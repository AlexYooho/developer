package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class WithdrawMessageRequestDTO {
    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("target_id")
    private Long targetId;
}
