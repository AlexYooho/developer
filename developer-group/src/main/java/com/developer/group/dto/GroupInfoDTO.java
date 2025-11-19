package com.developer.group.dto;

import com.developer.framework.enums.group.GroupMemberJoinTypeEnum;
import com.developer.framework.enums.group.GroupMemberRoleEnum;
import com.developer.framework.enums.group.GroupTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class GroupInfoDTO {

    private Long id;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_avatar")
    private String groupAvatar;

    @JsonProperty("group_owner_id")
    private Long groupOwnerId;

    @JsonProperty("group_type")
    private GroupTypeEnum groupType;

    @JsonProperty("max_member_count")
    private Integer maxMemberCount;

    @JsonProperty("member_count")
    private Integer memberCount;

    @JsonProperty("notice")
    private String notice;

    @JsonProperty("mute_all")
    private Boolean muteAll;

    @JsonProperty("member_role")
    private GroupMemberRoleEnum memberRole;

    @JsonProperty("group_member_alias")
    private String groupMemberAlias;

    @JsonProperty("join_time")
    private Date joinTime;

    @JsonProperty("join_type")
    private GroupMemberJoinTypeEnum joinType;

    @JsonProperty("is_muted")
    private Boolean isMuted;

    @JsonProperty("remark")
    private String remark;

}
