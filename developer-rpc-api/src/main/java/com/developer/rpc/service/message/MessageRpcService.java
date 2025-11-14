package com.developer.rpc.service.message;

import com.developer.framework.model.DeveloperResult;

public interface MessageRpcService {

    /*
    发送好友申请同意消息
     */
    DeveloperResult<Boolean> sendFriendApplyAcceptMessage(Long receiverId);

    /*
    发送好友申请拒绝消息
     */
    DeveloperResult<Boolean> sendFriendApplyRejectMessage(Long receiverId,String rejectReason);

    /*
    清理好友聊天记录
     */
    DeveloperResult<Boolean> clearFriendChatMessage(Long friendId);

}
