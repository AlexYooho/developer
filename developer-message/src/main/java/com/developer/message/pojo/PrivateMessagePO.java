package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("private_message")
public class PrivateMessagePO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送用户id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 接收用户id
     */
    @TableField("recv_id")
    private Long receiverId;

    /**
     * 发送内容
     */
    @TableField("content")
    private String messageContent;

    /**
     * 消息类型 0:文字 1:图片 2:文件 3:语音  10:撤回消息
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
}
