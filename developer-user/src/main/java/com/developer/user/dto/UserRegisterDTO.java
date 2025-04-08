package com.developer.user.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class UserRegisterDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    private String account;

    private String password;

    private String nickname;

    private Integer sex;

    private Integer verifyCode;

    private String email;

}
