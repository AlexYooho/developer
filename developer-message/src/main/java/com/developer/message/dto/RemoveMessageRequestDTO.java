package com.developer.message.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RemoveMessageRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("target_id")
    private Long targetId;

}
