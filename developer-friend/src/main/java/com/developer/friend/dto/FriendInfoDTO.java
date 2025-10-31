package com.developer.friend.dto;

import com.developer.friend.enums.AddFriendChannelEnum;
import com.developer.friend.enums.FriendStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FriendInfoDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nick_name")
    private String nickName;

    @JsonProperty("head_image")
    private String headImage;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("status")
    private FriendStatusEnum status;

    @JsonProperty("add_source")
    private AddFriendChannelEnum addSource;

    @JsonProperty("account")
    private String account;

    @JsonProperty("area")
    private String area;
}
