package com.developer.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetAccessTokenRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("password")
    private String password;
}
