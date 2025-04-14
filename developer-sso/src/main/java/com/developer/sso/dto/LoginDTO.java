package com.developer.sso.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class LoginDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("account")
    private String account;

    @JsonProperty("password")
    private String password;

}
