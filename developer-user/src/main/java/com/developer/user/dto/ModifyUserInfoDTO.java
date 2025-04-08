package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ModifyUserInfoDTO {

    @JsonProperty("serial_no")
    private String serialNo;

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
