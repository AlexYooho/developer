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

    public List<PrivateMessagePO> findCurrentUserMessage(Long minId, Long userId, List<Long> friendIds){
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        return this.lambdaQuery().gt(PrivateMessagePO::getId,minId)
                .ge(PrivateMessagePO::getSendTime,minDate)
                .ne(PrivateMessagePO::getMessageStatus, MessageStatusEnum.RECALL.code())
                .and(x->x.and(
                                z->z.eq(PrivateMessagePO::getSendId,userId)
                                        .in(PrivateMessagePO::getReceiverId,friendIds))
                        .or(q->q.eq(PrivateMessagePO::getReceiverId,userId)
                                .in(PrivateMessagePO::getSendId,friendIds)))
                .orderByAsc(PrivateMessagePO::getId)
                .last("limit 100").list();
    }

    public boolean UpdateStatus(Long friendId,Long userId,Integer messageStatus){
        return this.lambdaUpdate().eq(PrivateMessagePO::getSendId,friendId)
                .eq(PrivateMessagePO::getReceiverId,userId)
                .eq(PrivateMessagePO::getMessageStatus,MessageStatusEnum.SENDED.code())
                .set(PrivateMessagePO::getMessageStatus,messageStatus)
                .update();
    }

    public List<PrivateMessagePO> findPrivateMessageList(Long userId,Long friendId,Long pageIndex,Long pageSize){
        return this.lambdaQuery().and(a->a.and(b->b.eq(PrivateMessagePO::getSendId,userId).eq(PrivateMessagePO::getReceiverId,friendId))
                        .or(c->c.eq(PrivateMessagePO::getReceiverId,userId).eq(PrivateMessagePO::getSendId,friendId)))
                .ne(PrivateMessagePO::getMessageStatus,MessageStatusEnum.RECALL.code())
                .orderByDesc(PrivateMessagePO::getId)
                .last("limit "+pageIndex+","+pageSize).list();
    }

}
