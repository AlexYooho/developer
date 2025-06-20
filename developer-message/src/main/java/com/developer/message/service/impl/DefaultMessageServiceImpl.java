package com.developer.message.service.impl;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.*;
import com.developer.message.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DefaultMessageServiceImpl implements MessageService {

    public DeveloperResult defaultResult(){
        return DeveloperResult.error(SerialNoHolder.getSerialNo(),404,"错误的消息类型");
    }

    @Override
    public MessageMainTypeEnum messageMainType() {
        return null;
    }

    @Override
    public DeveloperResult<List<SendMessageResultDTO>> loadMessage(LoadMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> withdrawMessage(RecallMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return CompletableFuture.completedFuture(defaultResult());
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return CompletableFuture.completedFuture(defaultResult());
    }

    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return null;
    }
}
