package com.developer.rpc.dto.group.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SameGroupInfoResponseRpcDTO {

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_avatar")
    private String groupAvatar;

    @JsonProperty("group_member_count")
    private Integer groupMemberCount;

}
