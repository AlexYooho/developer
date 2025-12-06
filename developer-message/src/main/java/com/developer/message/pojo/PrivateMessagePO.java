package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@TableName("private_message")
public class PrivateMessagePO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /*
    较小的用户ID
     */
    @TableField("uid_a")
    private Long uidA;

    /*
    较大的用户ID
     */
    @TableField("uid_b")
    private Long uidB;

    /**
     * 发送用户id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 接收用户id
     */
    @TableField("receiver_id")
    private Long receiverId;

    /*
   会话内严格递增序列号（彻底解决时间乱序、时钟回拨问题）
    */
    @TableField("conv_seq")
    private Long convSeq;

    /*
    客户端生成的 ID，用于幂等
     */
    @TableField("client_msg_id")
    private String clientMsgId;

    /**
     * 发送内容
     */
    @TableField("content")
    private String messageContent;

    /**
     * 消息类型 0:文字 1:图片 2:文件 3:红包 4:转账
     */
    @TableField("type")
    private MessageContentTypeEnum messageContentType;

    /**
     * 状态
     */
    @TableField("status")
    private MessageStatusEnum messageStatus;

    /*
    已读状态（0 未读，1 已读）
     */
    @TableField("read_status")
    private Integer readStatus;

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
     * 点赞数
     */
    @TableField("like_count")
    private Long likeCount;

    /*
    扩展字段（JSON）
     */
    @TableField("extra")
    private String extra;

    /*
    是否逻辑删除
     */
    @TableField("visible_to_oneself")
    private Boolean visibleToOneself;

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
