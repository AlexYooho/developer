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
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 发送用户id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 发送用户昵称
     */
    @TableField("send_nick_name")
    private String sendNickName;

    /**
     * @用户列表
     */
    @TableField("at_user_ids")
    private String atUserIds;
    /**
     * 发送内容
     */
    @TableField("content")
    private String messageContent;

    /**
     * 消息类型 0:文字 1:图片 2:文件
     */
    @TableField("type")
    private Integer messageContentType;

    /**
     * 状态
     */
    @TableField("status")
    private Integer messageStatus;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;

    /**
     * 引用消息id
     */
    @TableField("reference_id")
    private Long referenceId;

    /**
     * 消息点赞数
     */
    @TableField("like_count")
    private Integer likeCount;
}
