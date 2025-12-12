package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UpsertConversationRequestDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("conv_type")
    private MessageConversationTypeEnum convType;

    @JsonProperty("target_id")
    private Long targetId;

    @JsonProperty("last_msg_seq")
    private Long lastMsgSeq;

    @JsonProperty("last_msg_id")
    private Long lastMsgId;

    @JsonProperty("last_msg_content")
    private String lastMsgContent;

    @JsonProperty("last_msg_type")
    private MessageContentTypeEnum lastMsgType;

    @JsonProperty("last_msg_time")
    private Date lastMsgTime;

    @JsonProperty("unread_count")
    private Integer unreadCount;

    @JsonProperty("pinned")
    private Boolean pinned;

    @JsonProperty("muted")
    private Boolean muted;

    @JsonProperty("deleted")
    private Boolean deleted;

    @JsonProperty("draft")
    private String draft;

}
