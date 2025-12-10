package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ReadMessageRequestDTO {

    /**
     * 会话目标
     */
    @JsonProperty("target_id")
    private Long targetId;
}
