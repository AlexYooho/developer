package com.developer.message.rpc;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.RemoveMessageRequestDTO;
import com.developer.message.service.factory.MessageTypeProcessorDispatchFactory;
import com.developer.rpc.service.message.MessageRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@RequiredArgsConstructor
public class MessageRpcProviderService implements MessageRpcService {

    private final MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    /*
    发送好友申请同意消息
     */
    @Override
    public DeveloperResult<Boolean> sendFriendApplyAcceptMessage(Long receiverId) {
        return messageTypeProcessorDispatchFactory.getInstance(MessageMainTypeEnum.PRIVATE_MESSAGE).friendApplyAcceptMessage(receiverId);
    }

    /*
    发送好友申请拒绝消息
     */
    @Override
    public DeveloperResult<Boolean> sendFriendApplyRejectMessage(Long receiverId,String rejectReason) {
        return messageTypeProcessorDispatchFactory.getInstance(MessageMainTypeEnum.PRIVATE_MESSAGE).friendApplyRejectMessage(receiverId,rejectReason);
    }

    /*
    清理好友聊天记录
     */
    @Override
    public DeveloperResult<Boolean> clearFriendChatMessage(Long friendId) {
        RemoveMessageRequestDTO dto = new RemoveMessageRequestDTO();
        dto.setTargetId(friendId);
        return messageTypeProcessorDispatchFactory.getInstance(MessageMainTypeEnum.PRIVATE_MESSAGE).deleteMessage(dto);
    }
}
