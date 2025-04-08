package com.developer.group.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class KickOutGroupRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("user_id")
    private Long userId;
}
