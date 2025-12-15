package com.developer.im.dto;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMUserInfoModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ChatMessageBodyDTO {

    /**
     * 指令类型
     */
    @JsonProperty("cmd")
    private IMCmdType cmd;

    /*
    目标id集合
     */
    @JsonProperty("target_ids")
    private List<Long> targetIds;

    /*
    消息体item
     */
    @JsonProperty("message_body_item")
    private ChatMessageBodyItemDTO bodyItem;


    @Data
    public static class ChatMessageBodyItemDTO{
        /**
         * 操作编号
         */
        @JsonProperty("serial_no")
        private String serialNo;

        /*
         * 发送方
         */
        @JsonProperty("sender")
        private IMUserInfoModel sender;

        /**
         * 消息主类型
         */
        @JsonProperty("message_main_type")
        private MessageConversationTypeEnum messageConversationTypeEnum;

        /**
         * 消息内容类型
         */
        @JsonProperty("message_content_type")
        private MessageContentTypeEnum messageContentTypeEnum;

        /**
         * 消息id
         */
        @JsonProperty("message_id")
        private Long messageId;

        /**
         * 消息内容
         */
        @JsonProperty("message_content")
        private String messageContent;

        /**
         * 发送时间
         */
        @JsonProperty("send_time")
        private Date sendTime;

        /**
         * at用户id集合
         */
        @JsonProperty("at_user_ids")
        private List<Long> atUserIds;

        /*
         群id
        */
        @JsonProperty("group_id")
        private Long groupId;

        /*
         * 目标用户信息
         */
        @JsonProperty("target_user_info")
        private IMUserInfoModel targetUserInfo;
    }
}
