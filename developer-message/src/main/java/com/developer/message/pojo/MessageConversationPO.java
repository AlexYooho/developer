package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@TableName("message_conversation")
public class MessageConversationPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    /*
    会话所属用户ID
     */
    private Long userId;

    /*
    会话类型
    0:私聊
    1:群聊
    2:系统通知（官方消息）
    3:客服会话
    4:机器人
    5:公众号/订阅号
    6:特殊会话（如语音助手）
     */
    @TableField("conv_type")
    private MessageMainTypeEnum convType;  // 0私聊 1群聊 ...

    /*
    会话目标
    conv_type=0（私聊） → 对方用户ID
    conv_type=1（群聊） → 群ID
    conv_type=2（系统通知） → 系统账号ID 或 0
    conv_type=3（客服） → 客服会话ID 或 店铺ID
     */
    @TableField("target_id")
    private Long targetId;

    /*
    最后一条消息的seq（私聊/群聊用）
     */
    @TableField("last_msg_seq")
    private Long lastMsgSeq = 0L;

    /*
    最后一条消息id
     */
    @TableField("last_msg_id")
    private Long lastMsgId;

    /*
    最后消息预览，如“你撤回了一条消息”
     */
    @TableField("last_msg_content")
    private String lastMsgContent;

    /*
    最后一条消息的类型
     */
    @TableField("last_msg_type")
    private MessageContentTypeEnum lastMsgType;

    /*
    最后活跃时间，用于排序
     */
    @TableField("last_msg_time")
    private Date lastMsgTime;

    /*
    当前用户的未读消息数
     */
    @TableField("unread_count")
    private Integer unreadCount = 0;

    /*
    是否置顶
     */
    @TableField("pinned")
    private Boolean pinned;

    /*
    是否免打扰
     */
    @TableField("muted")
    private Boolean muted;

    /*
    用户是否删除了该会话
     */
    @TableField("deleted")
    private Boolean deleted;

    /*
    草稿文本
     */
    @TableField("draft")
    private String draft;

    /*
    不同类型会话的专属字段，如群名称、头像、成员数等
     */
    @TableField("extra")
    private String extra;

    /*
    创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /*
    修改时间
     */
    @TableField("update_time")
    private Date updateTime;
}
