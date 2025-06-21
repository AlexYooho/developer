package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ModifyUserPasswordDTO {

    private String oldPassword;

    private String newPassword;

    private Integer verifyCode;

}
