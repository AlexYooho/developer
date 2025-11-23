package com.developer.message.service;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMessageAdapterService implements MessageService{

    @Override
    public MessageMainTypeEnum messageMainType() {
        return null;
    }

    @Override
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> withdrawMessage(RecallMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        return null;
    }

    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> friendApplyAcceptMessage(Long receiverId) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> friendApplyRejectMessage(Long receiverId,String rejectReason) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        return null;
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return null;
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return null;
    }

    @Override
    public DeveloperResult<Boolean> sendJoinGroupInviteMessage(List<Long> memberIds,String groupName,String inviterName,String groupAvatar) {
        return null;
    }
}
