package com.developer.friend.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MessageInsertDTO {

    private Long id;

    private Long groupId;

    private Long sendId;

    private String sendNickName;

    private String messageContent;

    private Integer messageContentType;

    private List<Long> atUserIds;

    private Integer messageStatus;

    private Date sendTime;

    private Long unReadCount;

    private Long readCount;

    private Long receiverId;

}
