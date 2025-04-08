package com.developer.message.dto;

import lombok.Data;

@Data
public class ReadMessageRequestDTO {
    /**
     * 操作编号
     */
    private String serialNo;

    /**
     * 已读消息id
     */
    private Long targetId;
}
