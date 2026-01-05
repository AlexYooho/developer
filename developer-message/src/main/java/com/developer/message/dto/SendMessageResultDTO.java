package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SendMessageResultDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("is_sent")
    private Boolean isSent;

    @JsonProperty("send_id")
    private Long sendId;

    @JsonProperty("conv_seq")
    private Long convSeq;

    @JsonProperty("message_content")
    private String messageContent;

    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    @JsonProperty("message_status")
    private MessageStatusEnum messageStatus;

    @JsonProperty("read_status")
    private Integer readStatus;

    @JsonProperty("send_nickname")
    private String sendNickName;

    @JsonProperty("send_time")
    private Date sendTime;

    @JsonProperty("reference_id")
    private Long referenceId;

    @JsonProperty("like_count")
    private Integer likeCount;
}
