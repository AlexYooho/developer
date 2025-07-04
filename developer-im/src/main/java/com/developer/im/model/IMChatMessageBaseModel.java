package com.developer.im.model;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class IMChatMessageBaseModel {

    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

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
    private MessageContentTypeEnum messageContentType;

    /**
     * 消息状态
     */
    @JsonProperty("message_status")
    private MessageStatusEnum messageStatus;

    /**
     * 接收者id
     */
    @JsonProperty("receiver_id")
    private Long receiverId;

    /**
     * 是否发送给自己的其他终端,默认true
     */
    @JsonProperty("send_self")
    private Boolean sendToSelf = true;

    /**
     * 发送人
     */
    @JsonProperty("sender_info")
    private IMUserInfoModel sender;

    /**
     * 发送时间
     */
    @JsonProperty("send_time")
    private Date sendTime;

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
     * 群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /**
     * 接收者id集合
     */
    @JsonProperty("receiver_ids")
    private List<Long> receiverIds = Collections.EMPTY_LIST;

    /**
     * at用户id集合
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;
}
