package com.developer.friend.dto;

import com.developer.framework.enums.friend.AddFriendChannelEnum;
import com.developer.framework.enums.friend.FriendStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FriendInfoDTO {

    @JsonProperty("id")
    private Long id;

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

    @JsonProperty("nick_name")
    private String nickName;

    @JsonProperty("head_image")
    private String headImage;

    @JsonProperty("head_image_thumb")
    private String headImageThumb;

    @JsonProperty("area")
    private String area;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("sex")
    private Integer sex;
}
