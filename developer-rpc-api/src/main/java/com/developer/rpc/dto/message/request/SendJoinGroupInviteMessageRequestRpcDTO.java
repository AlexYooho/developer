package com.developer.rpc.dto.message.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SendJoinGroupInviteMessageRequestRpcDTO implements Serializable {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("invite_member_ids")
    private List<Long> inviteMemberIds;

    @JsonProperty("group_avatar")
    private String groupAvatar;

    @JsonProperty("inviter_id")
    private Long inviterId;

    @JsonProperty("inviter_name")
    private String inviterName;

}
