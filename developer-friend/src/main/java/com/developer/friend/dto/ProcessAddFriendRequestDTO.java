package com.developer.friend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ProcessAddFriendRequestDTO {

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("is_agree")
    private Boolean isAgree;

    @JsonProperty("friend_remark")
    private String friendRemark;

    @JsonProperty("refuse_reason")
    private String refuseReason;

}
