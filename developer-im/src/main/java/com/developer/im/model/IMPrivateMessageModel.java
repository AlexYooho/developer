package com.developer.im.model;

import com.developer.framework.enums.MessageTerminalTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class IMPrivateMessageModel<T> {

    /**
     * 操作编号
     */
    private String serialNo;

    /**
     * 发送人
     */
    private IMUserInfoModel sender;

    /**
     * 接收者id
     */
    private Long receiverId;

    /**
     * 接收者终端类型,默认全部
     */
    private List<Integer> recvTerminals = MessageTerminalTypeEnum.codes();

    /**
     * 是否发送给自己的其他终端,默认true
     */
    private Boolean sendToSelf = true;

    /**
     * 是否需要回推发送结果,默认true
     */
    private Boolean sendResult = true;

    /**
     *  消息内容
     */
    private T data;

}