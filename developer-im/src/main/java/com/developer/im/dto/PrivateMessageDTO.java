package com.developer.im.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessageDTO {

    private Long id;

    private Long sendId;

    private Long receiverId;

    private String messageContent;

    private Integer messageContentType;

    private Integer messageStatus;

    private Date sendTime;
}
