package com.developer.message.dto;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class SendMessageResultDTO {
    private Long id;

    private Long sendId;

    private String messageContent;

    private MessageContentTypeEnum messageContentType;

    private MessageStatusEnum messageStatus;

    private String sendNickName;

    private Date sendTime;
}
