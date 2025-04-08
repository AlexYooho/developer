package com.developer.group.dto;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@Data
public class GroupInviteRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("friend_ids")
    private List<Long> friendIds;


}
