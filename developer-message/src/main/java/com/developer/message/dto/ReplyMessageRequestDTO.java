package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class ReplyMessageRequestDTO {

    /**
     * 接收人
     */
    @JsonProperty("receiver_id")
    private Long receiverId;

    /**
     * 消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /**
     * 消息主类型
     */
    @JsonProperty("message_main_type")
    private MessageMainTypeEnum messageMainType;

    /**
     * 消息内容类型
     */
    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    /**
     * 群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /**
     * @ 用户id
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;

    /**
     * 引用消息id
     */
    @JsonProperty("reference_id")
    private Long referenceId;
}
