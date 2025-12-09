package com.developer.group.dto;

import com.developer.framework.enums.group.GroupMemberRoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SelfJoinGroupInfoDTO {

    /**
     * 用户id
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /**
     * 群名称
     */
    @JsonProperty("group_name")
    private String groupName;

    /**
     * 群头像
     */
    @JsonProperty("group_avatar")
    private String groupAvatar;

    /**
     * 是否在群
     */
    @JsonProperty("quit")
    private Boolean quit;

    /**
     * 群昵称
     */
    @JsonProperty("member_alias")
    private String memberAlias;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private Date createTime;

    /*
   群角色
    */
    @JsonProperty("group_role")
    private GroupMemberRoleEnum groupRole;

}
