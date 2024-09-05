package com.developer.friend.dto;

import lombok.Data;

@Data
public class ProcessAddFriendRequestDTO {

    private Long friendId;

    private Boolean isAgree;

    private String friendRemark;

    private String refuseReason;

}
