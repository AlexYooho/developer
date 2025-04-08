package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class FindUserRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

}
