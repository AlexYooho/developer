package com.developer.user.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class FindUserRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

}
