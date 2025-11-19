package com.developer.group.dto.group;

import com.developer.framework.enums.group.GroupInviteTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModifyGroupInfoRequestDTO {
    @JsonProperty("serial_no")
    private String serialNo;

    /*
    群id
     */
    @JsonProperty("group_id")
    private Long groupId;

    /*
    群名称
     */
    @JsonProperty("group_name")
    private String groupName;

    /*
    群头像
     */
    @JsonProperty("group_avatar")
    private String groupAvatar;

    /*
    群主
     */
    @JsonProperty("owner_id")
    private Long ownerId;

    /*
    进群方式
     */
    @JsonProperty("invite_type")
    private GroupInviteTypeEnum inviteType;

    /*
    群公告
     */
    @JsonProperty("notice")
    private String notice;

    /*
    全体禁言
     */
    @JsonProperty("mute_all")
    private Boolean muteAll;
}
