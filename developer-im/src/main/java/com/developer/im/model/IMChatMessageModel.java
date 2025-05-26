package com.developer.im.model;

import lombok.Data;

import java.util.List;

/**
 * 消息接收者模型
 */
@Data
public class IMChatMessageModel extends IMProcessorBaseModel{

    /*
     * 发送方
     */
    private IMUserInfoModel sender;

    /*
     *  是否需要回调发送结果
     */
    private Boolean sendResult;

    /*
     * 推送消息体
     */
    private Object data;
}
