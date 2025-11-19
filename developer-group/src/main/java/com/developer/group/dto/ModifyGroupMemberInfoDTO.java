package com.developer.group.dto;

import com.developer.framework.enums.group.GroupMemberRoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ModifyGroupMemberInfoDTO {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("member_user_id")
    private Long memberUserId;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("member_role")
    private GroupMemberRoleEnum memberRole;

    @JsonProperty("is_muted")
    private Boolean isMuted;

    @JsonProperty("mute_end_time")
    private Date muteEndTime;

    @JsonProperty("last_read_msg_id")
    private Long lastReadMsgId;

    @JsonProperty("mute_notify")
    private Boolean muteNotify;

    @JsonProperty("quit")
    private Boolean quit;

}
