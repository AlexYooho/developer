package com.developer.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoveMessageRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("target_id")
    private Long targetId;

}
