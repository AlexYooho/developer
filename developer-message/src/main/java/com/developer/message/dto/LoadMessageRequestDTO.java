package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.developer.framework.enums.common.TerminalTypeEnum;

@Data
public class LoadMessageRequestDTO {

    /*
    消息最小id
     */
    @JsonProperty("last_seq")
    private Long lastSeq;

    /*
    目标用户
     */
    @JsonProperty("target_id")
    private Long targetId;

    /*
    终端
     */
    @JsonProperty("terminal_type")
    private TerminalTypeEnum terminalType;
}
