package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class QueryHistoryMessageRequestDTO {
    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("target_id")
    private Long targetId;

    @JsonProperty("page")
    private Long page;

    @JsonProperty("size")
    private Long size;
}
