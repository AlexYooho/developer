package com.developer.im.dto;

import com.alibaba.fastjson.JSON;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMUserInfoModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PushMessageBodyDTO {

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
    @JsonProperty("message_receiver_list")
    private List<IMUserInfoModel> messageReceiverList;

    @JsonProperty("message_receiver_ids")
    private List<Long> messageReceiverIds;

    public <T> T parseData(Class<T> clazz){
        if(data==null || "".equals(data)){
            return null;
        }

        return JSON.parseObject(data.toString(), clazz);
    }

}
