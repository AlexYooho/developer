package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UserRegisterDTO {

    @JsonProperty("account")
    private String account;

    @JsonProperty("password")
    private String password;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("sex")
    private Integer sex;

    @JsonProperty("verify_code")
    private Integer verifyCode;

    @JsonProperty("email")
    private String email;

}
