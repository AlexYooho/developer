package com.developer.user.dto;

import lombok.Data;

@Data
public class ModifyUserInfoDTO {

    private Long id;

    private String username;

    private String nickname;

    private Integer sex;

    private Integer type;

    private String signature;

    private String headImage;

    private String headImageThumb;

    private Boolean online;

}
