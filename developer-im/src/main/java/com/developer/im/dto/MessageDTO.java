package com.developer.im.dto;

import com.developer.im.enums.IMTerminalType;
import com.developer.im.enums.MessageContentType;
import com.developer.im.enums.MessageMainType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MessageDTO implements Serializable {

    private MessageMainType messageMainType;

    private MessageContentType messageContentType;

    private Integer messageStatus;

    private IMTerminalType terminalType;

    private Long messageId;

    private Long groupId;

    private Long sendId;

    private String sendNickName;

    private String messageContent;

    private List<Long> receiverIds;

    private List<Long> atUserIds;

    private Date sendTime;
}

