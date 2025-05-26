package com.developer.im.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IMChatGroupMessageModel extends IMChatMessageModel{

    /*
     * 接收方
     */
    @JsonProperty("message_receiver_list")
    private List<IMUserInfoModel> messageReceiverList;

}
