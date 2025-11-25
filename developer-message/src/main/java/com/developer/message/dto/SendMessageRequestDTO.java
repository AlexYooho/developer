package com.developer.message.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.enums.message.MessageTerminalTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageRequestDTO {

    /**
     * 操作编号
     */
    @JsonProperty("serial_no")
    private String serialNo;

    /**
     * 接收人
     */
    @JsonProperty("receiver_id")
    private Long receiverId;

    /**
     * 消息内容
     */
    @JsonProperty("message_content")
    private String messageContent;

    /**
     * 消息主类型
     */
    @JsonProperty("message_main_type")
    private MessageMainTypeEnum messageMainType;

    /**
     * 消息内容类型
     */
    @JsonProperty("message_content_type")
    private MessageContentTypeEnum messageContentType;

    /**
     * 群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /**
     * @ 用户id
     */
    @JsonProperty("at_user_ids")
    private List<Long> atUserIds;

    /**
     * 引用消息id
     */
    @JsonProperty("reference_id")
    private Long referenceId;

    /**
     * 终端类型
     */
    @JsonProperty("terminal_type")
    private MessageTerminalTypeEnum terminalType;

}
