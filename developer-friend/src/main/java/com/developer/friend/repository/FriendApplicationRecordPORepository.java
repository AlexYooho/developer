package com.developer.friend.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.friend.enums.AddFriendStatusEnum;
import com.developer.friend.mappers.FriendApplicationRecordMapper;
import com.developer.friend.pojo.FriendApplicationRecordPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FriendApplicationRecordPORepository extends ServiceImpl<FriendApplicationRecordMapper, FriendApplicationRecordPO> {

    public FriendApplicationRecordPO findRecord(Long targetUserId,Long mainUserId){
        return this.lambdaQuery().eq(FriendApplicationRecordPO::getTargetUserId,targetUserId).eq(FriendApplicationRecordPO::getMainUserId,mainUserId).one();
    }

    public void updateStatus(Long targetUserId,Long mainUserId,Integer status){
        this.lambdaUpdate().eq(FriendApplicationRecordPO::getTargetUserId,targetUserId).eq(FriendApplicationRecordPO::getMainUserId,mainUserId).set(FriendApplicationRecordPO::getStatus,status).update();
    }

    public List<FriendApplicationRecordPO> findRecordByStatus(Long targetUserId, AddFriendStatusEnum status){
        return this.lambdaQuery().eq(FriendApplicationRecordPO::getTargetUserId,targetUserId).eq(FriendApplicationRecordPO::getStatus,status.code()).list();
    }

    public boolean updateStatusSentToViewed(Long targetUserId){
        return this.lambdaUpdate().eq(FriendApplicationRecordPO::getTargetUserId, targetUserId).eq(FriendApplicationRecordPO::getStatus, AddFriendStatusEnum.SENT.code()).set(FriendApplicationRecordPO::getStatus, AddFriendStatusEnum.VIEWED.code()).update();
    }
}
