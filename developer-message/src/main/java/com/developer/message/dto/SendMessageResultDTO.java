package com.developer.message.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SendMessageResultDTO {
    private Long id;

    private Long sendId;

    private String messageContent;

    private Integer messageContentType;

    private Integer messageStatus;

    private String sendNickName;

    private Date sendTime;
}
