package com.developer.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {

    private String account;

    private String password;

    private String nickname;

    private Integer sex;

}
