package com.developer.message.dto;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequestDTO {

    private Long receiverId;

    private String messageContent;

    private MessageMainTypeEnum messageMainType;

    private MessageContentTypeEnum messageContentType;

    private Long groupId;

    private List<Long> atUserIds;

}
