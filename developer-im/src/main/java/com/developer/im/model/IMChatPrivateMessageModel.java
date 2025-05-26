package com.developer.im.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMChatPrivateMessageModel extends IMChatMessageModel {

    /*
     * 接收方
     */
    @JsonProperty("message_receiver")
    private IMUserInfoModel messageReceiver;

}
