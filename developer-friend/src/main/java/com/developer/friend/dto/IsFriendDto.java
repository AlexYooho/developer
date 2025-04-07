package com.developer.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IsFriendDto {
    private String serialNo;

    private Long friendId;

    private Long userId;
}
