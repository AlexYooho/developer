package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.message.mappers.PrivateMessageMapper;
import com.developer.message.pojo.PrivateMessagePO;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class PrivateMessageRepository extends ServiceImpl<PrivateMessageMapper, PrivateMessagePO> {

    public List<PrivateMessagePO> getMessageListByUserId(Long minId, Long userId){
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        return this.lambdaQuery().gt(PrivateMessagePO::getId,minId)
                .ge(PrivateMessagePO::getSendTime,minDate)
                .ne(PrivateMessagePO::getMessageStatus, MessageStatusEnum.RECALL.code())
                .and(x->x.and(z->z.eq(PrivateMessagePO::getSendId,userId)).or(q->q.eq(PrivateMessagePO::getReceiverId,userId)))
                .orderByAsc(PrivateMessagePO::getId)
                .last("limit 100").list();
    }

    public void updateMessageStatus(Long friendId, Long userId, Integer messageStatus){
        this.lambdaUpdate().eq(PrivateMessagePO::getSendId, friendId)
                .eq(PrivateMessagePO::getReceiverId, userId)
                .eq(PrivateMessagePO::getMessageStatus, MessageStatusEnum.SENDED.code())
                .set(PrivateMessagePO::getMessageStatus, messageStatus)
                .update();
    }

    public void updateMessageStatus(List<Long> ids,MessageStatusEnum status){
        this.lambdaUpdate().in(PrivateMessagePO::getId,ids).set(PrivateMessagePO::getMessageStatus,status.code()).update();
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
