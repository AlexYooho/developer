package com.developer.rpc.dto.user.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoResponseRpcDTO implements Serializable {

    //@JsonProperty("user_id")
    private Long userId;

    //@JsonProperty("account")
    private String account;

    //@JsonProperty("user_name")
    private String userName;

    //@JsonProperty("nick_name")
    private String nickName;

    //@JsonProperty("avatar")
    private String avatar;

    //@JsonProperty("avatar_thumb")
    private String avatarThumb;

    //@JsonProperty("sex")
    private Integer sex;

    //@JsonProperty("area")
    private String area;

    //@JsonProperty("signature")
    private String signature;

    //@JsonProperty("last_login_time")
    private Date lastLoginTime;

}
