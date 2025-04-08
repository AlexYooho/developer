package com.developer.message.service;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.MessageLikeRequestDTO;

import java.util.concurrent.CompletableFuture;

public interface MessageLikeService {

    /**
     * 消息点赞
     *
     * @param req
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> like(MessageLikeRequestDTO req, MessageMainTypeEnum messageMainTypeEnum);

    /**
     * 取消点赞
     * @param req
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> unLike(MessageLikeRequestDTO req, MessageMainTypeEnum messageMainTypeEnum);

}
