package com.developer.friend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
public class BatchModifyFriendListRequestDTO {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("list")
    private List<FriendInfoDTO> list;

}
