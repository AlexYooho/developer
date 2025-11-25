package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.enums.message.MessageLikeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MessageLikeRecordPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * messageId
     */
    @TableField(value = "message_id")
    private Long messageId;

    /**
     * 消息类型
     */
    @TableField(value = "message_type")
    private MessageMainTypeEnum messageType;

    /**
     * 点赞用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 点赞状态:0-取消点赞,1-点赞
     */
    @TableField(value = "like_status")
    private MessageLikeEnum LikeStatus;

    /**
     * 点赞时间
     */
    @TableField(value = "like_time")
    private Date LikeTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date CreateTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date UpdateTime;
}
