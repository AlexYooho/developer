package com.developer.group.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class FindGroupMemberUserIdRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("group_id")
    private Long groupId;

}
