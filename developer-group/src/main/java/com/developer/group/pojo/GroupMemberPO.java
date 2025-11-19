package com.developer.group.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.group.GroupMemberJoinTypeEnum;
import com.developer.framework.enums.group.GroupMemberRoleEnum;
import lombok.Data;

import java.util.Date;

@Data
@TableName("group_member")
public class GroupMemberPO {

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
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     *  群内显示名称
     */
    @TableField("alias")
    private String alias;

    /*
    成员角色
   */
    @TableField("member_role")
    private GroupMemberRoleEnum memberRole;

    /*
    入群时间
     */
    @TableField("join_time")
    private Date joinTime;

    /*
    入群方式
     */
    @TableField("join_type")
    private GroupMemberJoinTypeEnum joinType;

    /*
    是否被禁言
     */
    @TableField("is_muted")
    private Boolean isMuted;

    /*
    禁言结束时间
     */
    @TableField("mute_end_time")
    private Date muteEndTime;

    /*
    最近阅读消息id
     */
    @TableField("last_read_msg_id")
    private Long lastReadMsgId;

    /*
    消息免打扰
     */
    @TableField("mute_notify")
    private Boolean muteNotify;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已退群聊
     */
    @TableField("quit")
    private Boolean quit;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /*
    修改时间
     */
    @TableField("update_time")
    private Date updateTime;

}
