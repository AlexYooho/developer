package com.developer.message.service.impl;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.dto.SendMessageResultDTO;
import com.developer.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DefaultMessageServiceImpl implements MessageService {

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    public DeveloperResult defaultResult(){
        return DeveloperResult.error(snowflakeNoUtil.getSerialNo(),404,"错误的消息类型");
    }

    @Override
    public MessageMainTypeEnum messageMainType() {
        return null;
    }

    @Override
    public DeveloperResult<List<SendMessageResultDTO>> loadMessage(Long minId) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> readMessage(Long friendId) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> recallMessage(Long id) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(Long friendId, Long page, Long size) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> deleteMessage(Long friendId) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, SendMessageRequestDTO dto) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> collectionMessage(Long messageId) {
        return defaultResult();
    }

    @Override
    public DeveloperResult<Boolean> forwardMessage(Long messageId, List<Long> userIdList) {
        return defaultResult();
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(Long messageId) {
        return CompletableFuture.completedFuture(defaultResult());
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(Long messageId) {
        return CompletableFuture.completedFuture(defaultResult());
    }

    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return null;
    }
}
