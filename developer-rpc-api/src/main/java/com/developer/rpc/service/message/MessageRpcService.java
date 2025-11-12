package com.developer.rpc.service.message;

import com.developer.framework.model.DeveloperResult;

public interface MessageRpcService {

    /*
    发送好友申请同意消息
     */
    DeveloperResult<Boolean> sendFriendApplyAcceptMessage();

    /*
    发送好友申请拒绝消息
     */
    DeveloperResult<Boolean> sendFriendApplyRejectMessage();

}
