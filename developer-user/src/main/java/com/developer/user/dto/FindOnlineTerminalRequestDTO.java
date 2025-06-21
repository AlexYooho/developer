package com.developer.user.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class FindOnlineTerminalRequestDTO {

    @JsonProperty("user_ids")
    private String userIds;

}
