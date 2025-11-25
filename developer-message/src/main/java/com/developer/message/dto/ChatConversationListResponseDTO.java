package com.developer.message.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ChatConversationListResponseDTO {

    private Long id;

    private Long targetId;

    private Integer convType;

    private String name;

    private String headImage;

    private String lastMsgContent;

    private Date lastMsgTime;

    private Integer unreadCount;

    private Boolean pinned;

    private Boolean muted;

}
