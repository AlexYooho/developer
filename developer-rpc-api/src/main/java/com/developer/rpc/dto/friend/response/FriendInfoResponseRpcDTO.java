package com.developer.rpc.dto.friend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class FriendInfoResponseRpcDTO implements Serializable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("tag_name")
    private String tagName;

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
