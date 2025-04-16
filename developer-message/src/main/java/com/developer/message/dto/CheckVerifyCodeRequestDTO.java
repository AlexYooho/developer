package com.developer.message.dto;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheckVerifyCodeRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("verify_code_type")
    private VerifyCodeTypeEnum verifyCodeTypeEnum;

    @JsonProperty("email_account")
    private String emailAccount;
}
