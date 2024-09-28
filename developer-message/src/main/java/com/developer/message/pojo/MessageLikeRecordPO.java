package com.developer.message.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.enums.MessageLikeEnum;
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
    @TableId(value = "message_id")
    private Long messageId;

    /**
     * 消息类型
     */
    @TableId(value = "message_type")
    private MessageMainTypeEnum messageType;

    /**
     * 点赞用户id
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 点赞状态:0-取消点赞,1-点赞
     */
    @TableId(value = "like_status")
    private MessageLikeEnum LikeStatus;

    /**
     * 点赞时间
     */
    @TableId(value = "like_time")
    private Date LikeTime;

    /**
     * 创建时间
     */
    @TableId(value = "create_time")
    private Date CreateTime;

    /**
     * 修改时间
     */
    @TableId(value = "update_time")
    private Date UpdateTime;
}
