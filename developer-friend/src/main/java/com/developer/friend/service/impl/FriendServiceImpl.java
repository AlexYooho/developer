package com.developer.friend.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.ProcessAddFriendRequestDTO;
import com.developer.friend.dto.SendAddFriendInfoRequestDTO;
import com.developer.friend.service.FriendService;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImpl implements FriendService {
    @Override
    public DeveloperResult findFriendByUserId(Long userId) {
        return null;
    }

    @Override
    public DeveloperResult findFriendList() {
        return null;
    }

    @Override
    public DeveloperResult isFriend(Long userId1, Long userId2) {
        return null;
    }

    @Override
    public DeveloperResult findFriend(Long friendId) {
        return null;
    }

    @Override
    public DeveloperResult sendAddFriendRequest(SendAddFriendInfoRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult processFriendRequest(ProcessAddFriendRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult deleteFriendByFriendId(Long friendId) {
        return null;
    }

    @Override
    public DeveloperResult findFriendAddRequestCount() {
        return null;
    }

    @Override
    public DeveloperResult findNewFriendList() {
        return null;
    }

    @Override
    public DeveloperResult updateAddFriendRecordStatus() {
        return null;
    }
}
