package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.message.mappers.GroupMessageMapper;
import com.developer.message.pojo.GroupMessagePO;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class GroupMessageRepository extends ServiceImpl<GroupMessageMapper, GroupMessagePO> {

    public List<GroupMessagePO> findHistoryMessage(Long groupId, Date createTime, Long id, Long size){
        return this.lambdaQuery().eq(GroupMessagePO::getGroupId,groupId)
                .gt(GroupMessagePO::getSendTime,createTime)
                .ne(GroupMessagePO::getMessageStatus, MessageStatusEnum.RECALL.code())
                .orderByDesc(GroupMessagePO::getId)
                .last("limit "+ id + ","+size).list();
    }

    public GroupMessagePO findLastMessage(Long groupId){
        return this.lambdaQuery().eq(GroupMessagePO::getGroupId,groupId).orderByDesc(GroupMessagePO::getId)
                .last("limit 1").select(GroupMessagePO::getId).one();
    }

}
