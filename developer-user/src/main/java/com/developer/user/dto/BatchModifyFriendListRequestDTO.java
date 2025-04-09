package com.developer.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchModifyFriendListRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("list")
    private List<FriendInfoDTO> list;

}
