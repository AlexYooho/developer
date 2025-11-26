package com.developer.framework.dto;

import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO implements Serializable {

    // --------------------------公共参数----------------------------------
    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 消息主类型
     */
    @JsonProperty("message_main_type")
    private MessageMainTypeEnum messageMainTypeEnum;

    /**
     * 消息内容类型
     */
    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentTypeEnum;

    /**
     * 消息状态
     */
    @JsonProperty("message_status")
    private MessageStatusEnum messageStatus;

    /**
     * 终端类型
     */
    @JsonProperty("terminal_type")
    private TerminalTypeEnum terminalType;

    /**
     * 消息id
     */
    @JsonProperty("message_id")
    private Long messageId;

    /**
     * 发送者id
     */
    @JsonProperty("send_id")
    private Long sendId;

    /**
     * 发送人昵称
     */
    @JsonProperty("send_nickname")
    private String sendNickName;

    /**
     * 消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /**
     * 发送时间
     */
    @JsonProperty("send_time")
    private Date sendTime;

    // --------------------------私聊参数----------------------------------
    /**
     * 好友用户id
     */
    @JsonProperty("friend_user_id")
    private Long friendUserId;

    // --------------------------群聊参数----------------------------------
    /**
     * 群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /**
     * 接收者id集合
     */
    @JsonProperty("receiver_group_member_user_ids")
    private List<Long> receiverIds;

    /**
     * at用户id集合
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;
}

