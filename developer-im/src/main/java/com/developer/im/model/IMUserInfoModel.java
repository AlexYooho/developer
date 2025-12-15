package com.developer.im.model;

import com.developer.framework.enums.common.TerminalTypeEnum;
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
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 消息终端
     */
    @JsonProperty("terminal")
    private TerminalTypeEnum terminal;

    /**
     * 发送人昵称
     */
    @JsonProperty("user_nickname")
    private String userNickname;

}
