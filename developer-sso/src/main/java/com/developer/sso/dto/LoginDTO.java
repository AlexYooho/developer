package com.developer.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("account")
    private String account;

    @JsonProperty("password")
    private String password;

}
