package com.developer.user.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class ModifyUserPasswordDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    private String oldPassword;

    private String newPassword;

    private Integer verifyCode;

}
