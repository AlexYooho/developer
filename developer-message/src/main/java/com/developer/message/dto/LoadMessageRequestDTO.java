package com.developer.message.dto;

import lombok.Data;

@Data
public class LoadMessageRequestDTO {
    /**
     * 操作编号
     */
    private String serialNo;

    /**
     * 消息最小id
     */
    private Long minId;
}
