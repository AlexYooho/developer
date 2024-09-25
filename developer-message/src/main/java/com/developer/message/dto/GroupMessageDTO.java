package com.developer.message.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupMessageDTO extends SendMessageResultDTO {
    //private Long id;

    //private Long sendId;

    //private String messageContent;

    //private Integer messageContentType;

    //private Integer messageStatus;

    //private String sendNickName;

    //private Date sendTime;

    private Long groupId;

    private List<Long> atUserIds;

    private Long unReadCount;

    private Long readCount;
}
