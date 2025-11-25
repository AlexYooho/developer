package com.developer.message.service.impl;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.MessageLikeRequestDTO;
import com.developer.message.dto.MessageLikeEventDTO;
import com.developer.message.pojo.GroupMessagePO;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MessageLikeServiceImpl implements MessageLikeService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> like(MessageLikeRequestDTO req, MessageMainTypeEnum messageMainTypeEnum) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String lockKey = RedisKeyConstant.MESSAGE_LIKE_KEY(messageMainTypeEnum, req.getMessageId(), userId);
        String messageLikeStatusKey = RedisKeyConstant.MESSAGE_LIKE_USER_KEY(messageMainTypeEnum, req.getMessageId(), userId);
        String messageLikeCountKey = RedisKeyConstant.MESSAGE_LIKE_MESSAGE_KEY(messageMainTypeEnum,req.getMessageId());
        if(!redisUtil.hasKey(messageLikeCountKey)){
            // 做缓存预热
            Long likeCount = getMessageLikeCount(req.getMessageId(),messageMainTypeEnum);
            if(likeCount>0){
                redisUtil.set(messageLikeCountKey,likeCount,1, TimeUnit.HOURS);
            }
        }
        // 生成分布式锁的key,基于messageId和userId
        RLock lock = redissonClient.getLock(lockKey);
        try{
            if(lock.tryLock(100,10,TimeUnit.SECONDS)){

                Boolean isLiked = redisUtil.get(messageLikeStatusKey, Boolean.class);

                if(isLiked!=null && isLiked){
                    return CompletableFuture.completedFuture(DeveloperResult.error(serialNo,"不能重复点赞"));
                }

                redisUtil.set(messageLikeStatusKey,true,24,TimeUnit.HOURS);
                redisUtil.increment(messageLikeCountKey,1L);
                redisUtil.setExpire(messageLikeCountKey,1,TimeUnit.HOURS);

                // 推送mq事件，更新数据库
                rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_CHAT_EXCHANGE,DeveloperMQConstant.MESSAGE_CHAT_ROUTING_KEY, ProcessorTypeEnum.MESSAGE_LIKE, MessageLikeEventDTO.builder().messageId(req.getMessageId()).userId(userId).messageMainTypeEnum(messageMainTypeEnum).build());

                return CompletableFuture.completedFuture(DeveloperResult.success(serialNo,true));
            }else{
                return CompletableFuture.completedFuture(DeveloperResult.error(serialNo,"点赞失败,请稍后重试"));
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(DeveloperResult.error(serialNo,"点赞失败,请稍后重试"));
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLike(MessageLikeRequestDTO req, MessageMainTypeEnum messageMainTypeEnum) {
        return null;
    }

    private Long getMessageLikeCount(Long messageId, MessageMainTypeEnum messageMainTypeEnum){
        Long likeCount = 0L;
        if(messageMainTypeEnum== MessageMainTypeEnum.PRIVATE_MESSAGE) {
            PrivateMessagePO groupMessagePO = privateMessageRepository.getById(messageId);
            likeCount = groupMessagePO.getLikeCount();
        }else if(messageMainTypeEnum== MessageMainTypeEnum.GROUP_MESSAGE){
            GroupMessagePO groupMessagePO = groupMessageRepository.getById(messageId);
            likeCount = groupMessagePO.getLikeCount();
        }
        return likeCount;
    }
}
