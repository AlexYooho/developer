package com.developer.message.service;

import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.MessageLikeRequestDTO;

import java.util.concurrent.CompletableFuture;

public interface MessageLikeService {

    /**
     * 消息点赞
     *
     * @param
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> like(Long messageId, MessageConversationTypeEnum messageConversationTypeEnum);

    /**
     * 取消点赞
     * @param
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> unLike(Long messageId, MessageConversationTypeEnum messageConversationTypeEnum);

}
