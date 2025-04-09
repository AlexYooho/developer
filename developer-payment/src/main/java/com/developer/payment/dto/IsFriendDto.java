package com.developer.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IsFriendDto {

    @JsonProperty("serial_no")
    private String serialNo;

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("user_id")
    private Long userId;
}
