package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.developer.framework.enums.MessageTerminalTypeEnum;

@Data
public class LoadMessageRequestDTO {

    /**
     * 消息最小id
     */
    @JsonProperty("min_id")
    private Long minId;

    /**
     * 终端类型
     */
    @JsonProperty("terminal_type")
    private MessageTerminalTypeEnum terminalType;
}
