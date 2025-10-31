package com.developer.rpc.DTO.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoRpcDTO implements Serializable {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("account")
    private String account;

    @JsonProperty("area")
    private String area;

}
