package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class ReplyMessageRequestDTO {

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
     * @ 用户id
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;


}
