package com.developer.im.model;

import com.developer.im.enums.SendCodeType;
import lombok.Data;

@Data
public class SendResultModel<T> {

    /**
     * 发送方
     */
    private IMUserInfoModel sender;

    /**
     * 接收方
     */
    private IMUserInfoModel receiver;

    /*
     * 发送状态 IMCmdType
     */
    private SendCodeType code;

    /*
     *  消息内容
     */
    private T data;

}
