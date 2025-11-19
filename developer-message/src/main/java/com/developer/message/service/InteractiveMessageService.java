package com.developer.message.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.CollectionMessageRequestDTO;
import com.developer.message.dto.ForwardMessageRequestDTO;
import com.developer.message.dto.MessageLikeRequestDTO;
import com.developer.message.dto.ReplyMessageRequestDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InteractiveMessageService {

    /**
     * 回复消息
     * @return
     */
    DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req);

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

    /*
    好友申请接受消息
     */
    DeveloperResult<Boolean> friendApplyAcceptMessage(Long receiverId);

    /*
    好友申请拒绝消息
     */
    DeveloperResult<Boolean> friendApplyRejectMessage(Long receiverId,String rejectReason);

    /*
    发送加入群聊邀请消息
     */
    DeveloperResult<Boolean> sendJoinGroupInviteMessage(List<Long> memberIds,String groupName,String inviterName,String groupAvatar);

}
