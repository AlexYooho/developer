package com.developer.friend.dto;

import com.developer.friend.enums.AddFriendChannelEnum;
import lombok.Data;

@Data
public class SendAddFriendInfoRequestDTO {

    private Long friendId;

    private String remark;

    private AddFriendChannelEnum addChannel;

}
