package com.developer.im.model;

import lombok.Data;

/**
 * 发送消息模型
 * @param <T>
 */
@Data
public class IMSendMessageInfoModel<T> {

    /**
     * 命令
     */
    private Integer cmd;

    /**
     * 发送消息体
     */
    private T data;

}
