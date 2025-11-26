package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.message.mappers.PrivateMessageMapper;
import com.developer.message.pojo.PrivateMessagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrivateMessageRepository extends ServiceImpl<PrivateMessageMapper, PrivateMessagePO> {

    public List<PrivateMessagePO> getMessageListByUserId(Long lastSeq, Long uidA, Long uidB){
        return baseMapper.findMessageList(lastSeq,uidA,uidB);
    }

    public void updateMessageStatus(Long friendId, Long userId, Integer messageStatus){
        this.lambdaUpdate().eq(PrivateMessagePO::getSendId, friendId)
                .eq(PrivateMessagePO::getReceiverId, userId)
                .eq(PrivateMessagePO::getMessageStatus, MessageStatusEnum.SENDED.code())
                .set(PrivateMessagePO::getMessageStatus, messageStatus)
                .update();
    }

    public void updateMessageStatus(List<Long> ids,MessageStatusEnum status){
        baseMapper.modifyMessageStatus(status,ids);
    }

    public List<PrivateMessagePO> getHistoryMessageList(Long userId,Long friendId,Long pageIndex,Long pageSize){
        return this.lambdaQuery().and(a->a.and(b->b.eq(PrivateMessagePO::getSendId,userId).eq(PrivateMessagePO::getReceiverId,friendId))
                        .or(c->c.eq(PrivateMessagePO::getReceiverId,userId).eq(PrivateMessagePO::getSendId,friendId)))
                .ne(PrivateMessagePO::getMessageStatus,MessageStatusEnum.RECALL.code())
                .orderByDesc(PrivateMessagePO::getId)
                .last("limit "+pageIndex+","+pageSize).list();
    }

    public boolean deleteChatMessage(Long userId, Long friendId){
       return this.lambdaUpdate().eq(PrivateMessagePO::getSendId, userId)
                .eq(PrivateMessagePO::getReceiverId, friendId)
                .remove();
    }

}
