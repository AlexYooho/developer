package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.mappers.MessageLikeRecordMapper;
import com.developer.message.pojo.MessageLikeRecordPO;
import org.springframework.stereotype.Repository;

@Repository
public class MessageLikeRecordRepository extends ServiceImpl<MessageLikeRecordMapper, MessageLikeRecordPO> {

    public MessageLikeRecordPO findLikeRecord(Long messageId, Long userId, MessageMainTypeEnum messageType){
        return this.lambdaQuery().eq(MessageLikeRecordPO::getMessageId,messageId).eq(MessageLikeRecordPO::getUserId,userId).eq(MessageLikeRecordPO::getMessageType,messageType).one();
    }

}
