package com.developer.friend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("friend")
public class FriendPO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 好友id
     */
    @TableField("friend_id")
    private Long friendId;

    /**
     * 用户昵称
     */
    @TableField("friend_nick_name")
    private String friendNickName;

    /**
     * 用户头像
     */
    @TableField("friend_head_image")
    private String friendHeadImage;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;
}
