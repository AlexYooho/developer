package com.developer.message.service;

import com.developer.framework.model.DeveloperResult;

public interface FriendService {

    DeveloperResult<Boolean> isFriend(Long userId,Long friendId);

}
