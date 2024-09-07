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
@TableName("group_message_member_receive_record")
public class GroupMessageMemberReceiveRecordPO {
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
     * 发送者id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 接收者id
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 消息id
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 发送时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 发送时间
     */
    @TableField("update_time")
    private Date updateTime;
}
