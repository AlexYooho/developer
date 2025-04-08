package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ReadMessageRequestDTO {
    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 已读消息id
     */
    @JsonProperty("target_id")
    private Long targetId;
}
