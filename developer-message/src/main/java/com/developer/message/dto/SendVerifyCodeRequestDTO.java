package com.developer.message.dto;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SendVerifyCodeRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("verify_code_type")
    private VerifyCodeTypeEnum verifyCodeType;

}
