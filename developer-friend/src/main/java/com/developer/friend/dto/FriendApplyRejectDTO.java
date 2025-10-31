package com.developer.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FriendApplyRejectDTO {
    @JsonProperty("friend_remark")
    private String friendRemark;

    @JsonProperty("refuse_reason")
    private String refuseReason;
}
