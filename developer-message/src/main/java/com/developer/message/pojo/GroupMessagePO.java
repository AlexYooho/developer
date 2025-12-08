package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("group_message")
public class GroupMessagePO {
    /*
    id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /*
    群id
     */
    @TableField("group_id")
    private Long groupId;

    /*
    群内消息序列号
     */
    @TableField("msg_seq")
    private Long msgSeq;

    /*
    发送用户id
     */
    @TableField("sender_id")
    private Long sendId;

    /*
    发送时昵称
     */
    @TableField("sender_nick")
    private String sendNickName;

    /*
    发送者角色
     */
    @TableField("sender_role")
    private Integer senderRole;

    /**
     * 消息类型 0:文字 1:图片 2:文件
     */
    @TableField("type")
    private Integer messageContentType;

    /**
     * 发送内容
     */
    @TableField("content")
    private String messageContent;

    /*
    回复的消息id
     */
    @TableField("reference_id")
    private Long referenceId;

    /*
    @用户列表
     */
    @TableField("at_user_ids")
    private String atUserIds;

    /**
     * 状态
     */
    @TableField("status")
    private Integer messageStatus;

    /*
    删除逻辑
     */
    @TableField("deleted")
    private Boolean deleted;

    /*
    点赞数
     */
    @TableField("like_count")
    private Long likeCount;

    /*
    已读数
     */
    @TableField("read_count")
    private Long readCount;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;

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
