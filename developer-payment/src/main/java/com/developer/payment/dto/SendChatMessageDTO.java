package com.developer.payment.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SendChatMessageDTO implements Serializable {

    /**
     * 接收人
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息主类型
     */
    private MessageConversationTypeEnum messageMainType;

    /**
     * 消息内容类型
     */
    private MessageContentTypeEnum messageContentType;

    /**
     * 群id
     */
    private Long groupId;

}
