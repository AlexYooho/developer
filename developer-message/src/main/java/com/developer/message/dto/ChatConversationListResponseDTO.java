package com.developer.message.dto;

import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ChatConversationListResponseDTO {

    @JsonProperty("id")
    private Long id;

    /*
    会话对象id
     */
    @JsonProperty("target_id")
    private Long targetId;

    /*
    会话类型
     */
    @JsonProperty("conv_type")
    private MessageConversationTypeEnum convType;

    /*
    会话对象名字
     */
    @JsonProperty("name")
    private String name;

    /*
    会话对象头像
     */
    @JsonProperty("head_image")
    private String headImage;

    /*
    最后一次聊天内容
     */
    @JsonProperty("last_msg_content")
    private String lastMsgContent;

    /*
    最后一次聊天时间
     */
    @JsonProperty("last_msg_time")
    private Date lastMsgTime;

    /*
    未读数
     */
    @JsonProperty("unread_count")
    private Integer unreadCount;

    /*
    是否置顶
     */
    @JsonProperty("pinned")
    private Boolean pinned;

    /*
    是否免打扰
     */
    @JsonProperty("muted")
    private Boolean muted;

}
