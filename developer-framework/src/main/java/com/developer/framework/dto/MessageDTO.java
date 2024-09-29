package com.developer.framework.dto;

import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class MessageDTO implements Serializable {

    private MessageMainTypeEnum messageMainTypeEnum;

    private MessageContentTypeEnum messageContentTypeEnum;

    private Integer messageStatus;

    private IMTerminalTypeEnum terminalType;

    private Long messageId;

    private Long groupId;

    private Long sendId;

    private String sendNickName;

    private String messageContent;

    private List<Long> receiverIds;

    private List<Long> atUserIds;

    private Date sendTime;
}

