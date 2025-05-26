package com.developer.im.model;

import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IMUserInfoModel {

    /**
     * 发送人id
     */
    @JsonProperty("sender_id")
    private Long senderId;

    /**
     * 消息终端
     */
    @JsonProperty("terminal")
    private MessageTerminalTypeEnum terminal;

    /**
     * 发送人昵称
     */
    @JsonProperty("sender_nickname")
    private String senderNickname;

}
