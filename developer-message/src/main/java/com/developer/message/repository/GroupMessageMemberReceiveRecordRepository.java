package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.GroupMessageMemberReceiveRecordMapper;
import com.developer.message.pojo.GroupMessageMemberReceiveRecordPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupMessageMemberReceiveRecordRepository extends ServiceImpl<GroupMessageMemberReceiveRecordMapper, GroupMessageMemberReceiveRecordPO> {

    public List<GroupMessageMemberReceiveRecordPO> findCurGroupUnreadRecordList(Long groupId, Long receiverId){
        return this.lambdaQuery().eq(GroupMessageMemberReceiveRecordPO::getStatus,0).eq(GroupMessageMemberReceiveRecordPO::getGroupId,groupId).eq(GroupMessageMemberReceiveRecordPO::getReceiverId,receiverId).list();
    }

    public List<GroupMessageMemberReceiveRecordPO> findAllUnreadMessageList(Long receiverId){
        return this.lambdaQuery().eq(GroupMessageMemberReceiveRecordPO::getReceiverId,receiverId).eq(GroupMessageMemberReceiveRecordPO::getStatus,0).list();
    }

    public List<GroupMessageMemberReceiveRecordPO> findAllMessageBySendId(Long sendId){
        return this.lambdaQuery().eq(GroupMessageMemberReceiveRecordPO::getSendId,sendId).list();
    }

}
