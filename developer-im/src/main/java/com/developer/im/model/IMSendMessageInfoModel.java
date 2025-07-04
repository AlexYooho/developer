package com.developer.im.model;

import com.developer.im.enums.IMCmdType;
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
    private IMCmdType cmd;

    /**
     * 发送消息体
     */
    private T data;

}
