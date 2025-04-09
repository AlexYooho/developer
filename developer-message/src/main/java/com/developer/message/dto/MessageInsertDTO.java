package com.developer.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MessageInsertDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("send_id")
    private Long sendId;

    @JsonProperty("send_nickname")
    private String sendNickName;

    @JsonProperty("message_content")
    private String messageContent;

    @JsonProperty("message_content_type")
    private Integer messageContentType;

    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;

    @JsonProperty("message_status")
    private Integer messageStatus;

    @JsonProperty("send_time")
    private Date sendTime;

    @JsonProperty("unread_count")
    private Long unReadCount;

    @JsonProperty("read_count")
    private Long readCount;

    @JsonProperty("receiver_id")
    private Long receiverId;

}
