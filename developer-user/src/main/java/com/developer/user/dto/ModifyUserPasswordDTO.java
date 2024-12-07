package com.developer.user.dto;

import lombok.Data;

@Data
public class ModifyUserPasswordDTO {

    private String oldPassword;

    private String newPassword;

    private Integer verifyCode;

}
