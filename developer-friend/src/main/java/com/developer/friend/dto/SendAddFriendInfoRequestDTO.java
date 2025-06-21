package com.developer.friend.dto;

import com.developer.friend.enums.AddFriendChannelEnum;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SendAddFriendInfoRequestDTO {
    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("add_channel")
    private AddFriendChannelEnum addChannel;

}
