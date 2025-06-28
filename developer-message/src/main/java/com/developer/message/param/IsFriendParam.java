package com.developer.message.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IsFriendParam {

    @JsonProperty("friend_id")
    private Long friendId;

    @JsonProperty("user_id")
    private Long userId;

}
