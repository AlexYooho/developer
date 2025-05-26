package com.developer.im.model;

import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class IMChatMessageBaseModel<T> {

    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 发送人
     */
    @JsonProperty("sender_info")
    private IMUserInfoModel sender;

    /**
     *  消息内容
     */
    @JsonProperty("data")
    private T data;

    /**
     * 是否发送给自己的其他终端,默认true
     */
    @JsonProperty("send_to_self")
    private Boolean sendToSelf = true;

    /**
     * 是否需要回推发送结果,默认true
     */
    @JsonProperty("send_result")
    private Boolean sendResult = true;

    /**
     * 接收者终端类型,默认全部
     */
    @JsonProperty("receive_terminals")
    private List<Integer> receiveTerminals = MessageTerminalTypeEnum.codes();

    /**
     * 消息id
     */
    @JsonProperty("message_id")
    private Long messageId;

    /**
     * 消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /**
     * 消息内容类型
     */
    @JsonProperty("message_content_type")
    private Integer messageContentType;
}
