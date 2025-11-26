package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.developer.framework.enums.message.MessageTerminalTypeEnum;

@Data
public class LoadMessageRequestDTO {

    /**
     * 消息最小id
     */
    @JsonProperty("min_id")
    private Long minId;

    /*
    目标用户
     */
    @JsonProperty("target_id")
    private Long targetId;
}
