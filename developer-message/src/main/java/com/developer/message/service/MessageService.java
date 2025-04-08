package com.developer.message.service;

import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {

    /**
     * 消息主体类型
     * @return
     */
    MessageMainTypeEnum messageMainType();

    /**
     * 加载消息
     * @param minId
     * @return
     */
    DeveloperResult<List<SendMessageResultDTO>> loadMessage(LoadMessageRequestDTO req);

    /**
     * 发送消息
     * @param req
     * @return
     */
    DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req);

    /**
     * 已读消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req);

    /**
     * 撤回消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> recallMessage(RecallMessageRequestDTO req);

    /**
     * 查询历史记录
     * @param friendId
     * @param page
     * @param size
     * @return
     */
    DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req);

    /**
     * 新增消息
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto);

    /**
     * 删除消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req);

    /**
     * 回复消息
     * @return
     */
    DeveloperResult<Boolean> replyMessage(Long id,ReplyMessageRequestDTO req);

    /**
     * 收藏消息
     * @return
     */
    DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req);

    /**
     * 转发消息
     * @return
     */
    DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req);

    /**
     * 消息点赞
     *
     * @param req
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req);

    /**
     * 取消点赞
     * @param req
     * @return
     */
    CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req);

    /**
     * 是否支付类型消息
     * @param messageContentTypeEnum
     * @return
     */
    Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum);
}
