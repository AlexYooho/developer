package com.developer.user.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class FindOnlineTerminalRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("user_ids")
    private String userIds;

}
