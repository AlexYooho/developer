package com.developer.im.model;

import lombok.Data;

import java.util.List;

/**
 * 消息接收者模型
 */
@Data
public class IMRecvInfoModel {

    /*
     * 命令类型 IMCmdType
     */
    private Integer cmd;

    /*
     * 发送方
     */
    private IMUserInfoModel sender;

    /*
     * 接收方用户列表
     */
    List<IMUserInfoModel> receivers;

    /*
     *  是否需要回调发送结果
     */
    private Boolean sendResult;

    /*
     * 推送消息体
     */
    private Object data;


}
