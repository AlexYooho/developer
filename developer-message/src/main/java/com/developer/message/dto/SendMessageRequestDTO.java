package com.developer.message.dto;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageRequestDTO {

    /**
     * 接收人
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息主类型
     */
    private MessageMainTypeEnum messageMainType;

    /**
     * 消息内容类型
     */
    private MessageContentTypeEnum messageContentType;

    /**
     * 群id
     */
    private Long groupId;

    /**
     * @ 用户id
     */
    private List<Long> atUserIds;

    /**
     * 引用消息id
     */
    private Long referenceId;

}
