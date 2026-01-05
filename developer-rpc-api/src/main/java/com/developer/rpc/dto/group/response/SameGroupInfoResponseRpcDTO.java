package com.developer.rpc.dto.group.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SameGroupInfoResponseRpcDTO implements Serializable {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_avatar")
    private String groupAvatar;

    @JsonProperty("group_member_count")
    private Integer groupMemberCount;

}
