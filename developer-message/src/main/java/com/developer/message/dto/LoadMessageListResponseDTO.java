package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class LoadMessageListResponseDTO {

    /*
    消息id
     */
    @JsonProperty("id")
    private Long id;

    /*
    发送者id
     */
    @JsonProperty("send_id")
    private Long sendId;

    /**
     * 接收用户id
     */
    @JsonProperty("receiver_id")
    private Long receiverId;

    /*
    接受群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /*
    消息序列号
     */
    @JsonProperty("conv_seq")
    private Long convSeq;

    /*
    消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /*
    消息类型
     */
    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    /*
    消息状态
     */
    @JsonProperty("message_status")
    private MessageStatusEnum messageStatus;

    /*
    已读状态（0 未读，1 已读）
     */
    @JsonProperty("read_status")
    private Integer readStatus;

    /*
    发送者昵称
     */
    @JsonProperty("send_nickname")
    private String sendNickName;

    /*
    发送时间
     */
    @JsonProperty("send_time")
    private Date sendTime;

    /**
     * 引用消息id
     */
    @JsonProperty("reference_id")
    private Long referenceId;

    /**
     * 点赞数
     */
    @JsonProperty("like_count")
    private Long likeCount;

    @JsonProperty("at_user_ids")
    private String atUserIds;

    @JsonProperty("un_read_count")
    private Long unReadCount;

}
