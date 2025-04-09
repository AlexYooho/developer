package com.developer.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshAccessTokenRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("refresh_token")
    private String refreshToken;

}
