package com.developer.friend.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class FindFriendRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("friend_id")
    private Long friendId;

}
