package com.developer.im.model;

import com.developer.im.enums.IMCmdType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMChatPrivateMessageModel {

    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 指令类型
     */
    @JsonProperty("cmd")
    private IMCmdType cmd;

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

    /*
     * 接收方
     */
    @JsonProperty("message_receiver")
    private IMUserInfoModel messageReceiver;

}
