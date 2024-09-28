package com.developer.message.service;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;

import java.util.concurrent.CompletableFuture;

public interface MessageLikeService {

    /**
     * 消息点赞
     *
     * @param messageId
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> like(Long messageId, MessageMainTypeEnum messageMainTypeEnum);

    /**
     * 取消点赞
     * @param messageId
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> unLike(Long messageId, MessageMainTypeEnum messageMainTypeEnum);

}
