package com.developer.group.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.group.GroupInviteTypeEnum;
import com.developer.framework.enums.group.GroupTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("group_info")
public class GroupInfoPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群名字
     */
    @TableField("group_name")
    private String groupName;

    /*
    群头像
     */
    @TableField("group_avatar")
    private String groupAvatar;

    /**
     * 群主id
     */
    @TableField("owner_id")
    private Long ownerId;

    /*
    群类型
     */
    @TableField("group_type")
    private GroupTypeEnum groupType;

    /*
    入群验证：0-直接加入、1-群主管理员审核、2-禁止加群
     */
    @TableField("invite_type")
    private GroupInviteTypeEnum inviteType;

    /*
    最大群成员数量
     */
    @TableField("max_member_count")
    private Integer maxMemberCount;

    /*
    群成员数量
     */
    @TableField("member_count")
    private Integer memberCount;

    /**
     * 群公告
     */
    @TableField("notice")
    private String notice;

    /*
    全体禁言
     */
    @TableField("mute_all")
    private Boolean muteAll;

    /**
     * 是否已删除
     */
    @TableField("deleted")
    private Boolean deleted;

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

    /*
    备注
     */
    @TableField("remark")
    private String remark;

}
