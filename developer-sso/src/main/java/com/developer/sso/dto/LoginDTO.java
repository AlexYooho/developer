package com.developer.sso.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class LoginDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    private String account;

    private String password;

}
