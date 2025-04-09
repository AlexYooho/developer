package com.developer.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindGroupRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("group_id")
    private Long groupId;

}
