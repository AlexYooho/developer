package com.developer.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IsFriendDto {

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("user_id")
    private Long userId;
}
