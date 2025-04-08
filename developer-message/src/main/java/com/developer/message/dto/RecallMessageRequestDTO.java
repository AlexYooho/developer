package com.developer.message.dto;

import lombok.Data;

@Data
public class RecallMessageRequestDTO {
    /**
     * 操作编号
     */
    private String serialNo;

    /**
     * 消息id
     */
    private Long messageId;
}
