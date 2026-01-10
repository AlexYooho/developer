package com.developer.message.dto;

import com.developer.framework.enums.message.MessageConversationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageLikeEventDTO {

    private Long userId;

    private Long messageId;

    private MessageConversationTypeEnum messageConversationTypeEnum;

}
