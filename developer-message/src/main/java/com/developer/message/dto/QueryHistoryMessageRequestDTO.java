package com.developer.message.dto;

import lombok.Data;

@Data
public class QueryHistoryMessageRequestDTO {
    /**
     * 操作编号
     */
    private String serialNo;

    private Long targetId;

    private Long page;

    private Long size;
}
