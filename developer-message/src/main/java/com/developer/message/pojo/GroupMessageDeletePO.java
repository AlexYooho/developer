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
@TableName("group_message_delete")
public class GroupMessageDeletePO {
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
    群内消息id
     */
    @TableField("msg_id")
    private Long msgId;

    /*
    群内消息序列号
     */
    @TableField("msg_seq")
    private Long msgSeq;

    /*
    谁删了这条消息
     */
    @TableField("user_id")
    private Long userId;

    /*
    删除时间
     */
    @TableField("delete_time")
    private Date deleteTime;
}
