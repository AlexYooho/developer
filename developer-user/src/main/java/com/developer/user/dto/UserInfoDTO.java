package com.developer.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoDTO {

    private Long id;

    private String username;

    private String nickname;

    private Integer sex;

    private Integer type;

    private String signature;

    private String headImage;

    private String headImageThumb;

    private Boolean online;

    @JsonProperty("account")
    private String account;

    @JsonProperty("area")
    private String area;

}
