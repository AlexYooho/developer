package com.developer.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerifyAccessTokenRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("token")
    private String token;

}
