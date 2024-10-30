package com.developer.payment.dto;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendChatMessageDTO {

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
    private MessageMainTypeEnum messageMainType;

    /**
     * 消息内容类型
     */
    private MessageContentTypeEnum messageContentType;

    /**
     * 群id
     */
    private Long groupId;

}
