package com.developer.message.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IsFriendParam {

    private String serialNo;

    private Long friendId;

    private Long userId;

}
