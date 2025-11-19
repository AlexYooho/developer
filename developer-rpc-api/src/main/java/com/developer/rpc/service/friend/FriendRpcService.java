package com.developer.rpc.service.friend;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.dto.friend.response.FriendInfoResponseRpcDTO;

import java.util.List;

public interface FriendRpcService {

    /*
    获取好友列表
     */
    DeveloperResult<List<FriendInfoResponseRpcDTO>> findFriends();

}
