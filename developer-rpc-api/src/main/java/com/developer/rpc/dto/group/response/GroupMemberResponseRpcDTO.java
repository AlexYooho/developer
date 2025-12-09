package com.developer.rpc.dto.group.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GroupMemberResponseRpcDTO {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("member_user_id")
    private Long memberUserId;

    @JsonProperty("member_user_name")
    private String memberUserName;

}
