package com.developer.im.model;

import com.developer.im.enums.IMCmdType;
import lombok.Data;

/**
 * 发送消息模型
 */
@Data
public class IMMessageBodyModel {

    /**
     * 命令
     */
    private IMCmdType cmd;

    /**
     * 发送消息体
     */
    private Object data;

}
