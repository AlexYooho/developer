package com.developer.message.dto;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SendMessageResultDTO {
//    private Long id;
//
//    private Long sendId;
//
//    private String messageContent;
//
//    private MessageContentTypeEnum messageContentType;
//
//    private MessageStatusEnum messageStatus;
//
//    private String sendNickName;
//
//    private Date sendTime;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("send_id")
    private Long sendId;

    @JsonProperty("message_content")
    private String messageContent;

    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    @JsonProperty("message_status")
    private MessageStatusEnum messageStatus;

    @JsonProperty("send_nickname")
    private String sendNickName;

    @JsonProperty("send_time")
    private Date sendTime;
}
