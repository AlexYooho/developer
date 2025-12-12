package com.developer.message.rpc;

import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.RemoveMessageRequestDTO;
import com.developer.message.service.factory.MessageTypeProcessorDispatchFactory;
import com.developer.rpc.dto.message.request.SendJoinGroupInviteMessageRequestRpcDTO;
import com.developer.rpc.service.message.MessageRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class MessageRpcProviderService implements MessageRpcService {

    private final MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    /*
    发送好友申请同意消息
     */
    @Override
    public DeveloperResult<Boolean> sendFriendApplyAcceptMessage(Long receiverId) {
        return messageTypeProcessorDispatchFactory.getInstance(MessageConversationTypeEnum.PRIVATE_MESSAGE).friendApplyAcceptMessage(receiverId);
    }

    /*
    发送好友申请拒绝消息
     */
    @Override
    public DeveloperResult<Boolean> sendFriendApplyRejectMessage(Long receiverId,String rejectReason) {
        return messageTypeProcessorDispatchFactory.getInstance(MessageConversationTypeEnum.PRIVATE_MESSAGE).friendApplyRejectMessage(receiverId,rejectReason);
    }

    /*
    清理好友聊天记录
     */
    @Override
    public DeveloperResult<Boolean> clearFriendChatMessage(Long friendId) {
        RemoveMessageRequestDTO dto = new RemoveMessageRequestDTO();
        dto.setTargetId(friendId);
        return messageTypeProcessorDispatchFactory.getInstance(MessageConversationTypeEnum.PRIVATE_MESSAGE).deleteMessage(dto);
    }

    /*
    发送入群邀请消息
     */
    @Override
    public DeveloperResult<Boolean> sendJoinGroupInviteMessage(SendJoinGroupInviteMessageRequestRpcDTO dto) {
        return messageTypeProcessorDispatchFactory.getInstance(MessageConversationTypeEnum.PRIVATE_MESSAGE).sendJoinGroupInviteMessage(dto.getInviteMemberIds(),dto.getGroupName(),dto.getInviterName(),dto.getGroupAvatar());
    }

    /*
    发送退出群聊消息
     */
    @Override
    public DeveloperResult<Boolean> sendQuitGroupChatMessage(Long groupId) {
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    踢出群聊消息
     */
    @Override
    public DeveloperResult<Boolean> sendKickGroupMessage(Long groupId) {
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
