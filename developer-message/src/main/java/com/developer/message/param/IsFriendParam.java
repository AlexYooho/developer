package com.developer.message.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IsFriendParam {

    private String serialNo;

    private Long friendId;

    private Long userId;

}
