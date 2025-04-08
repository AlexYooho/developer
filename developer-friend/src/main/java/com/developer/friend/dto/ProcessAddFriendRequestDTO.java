package com.developer.friend.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class ProcessAddFriendRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("is_agree")
    private Boolean isAgree;

    @JsonProperty("friend_remark")
    private String friendRemark;

    @JsonProperty("refuse_reason")
    private String refuseReason;

}
