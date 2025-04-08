package com.developer.friend.dto;

import com.developer.friend.enums.AddFriendChannelEnum;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class SendAddFriendInfoRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("add_channel")
    private AddFriendChannelEnum addChannel;

}
