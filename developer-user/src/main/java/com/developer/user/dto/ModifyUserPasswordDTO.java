package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ModifyUserPasswordDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    private String oldPassword;

    private String newPassword;

    private Integer verifyCode;

}
