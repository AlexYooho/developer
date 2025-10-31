package com.developer.friend.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.friend.enums.AddFriendStatusEnum;
import com.developer.friend.mappers.FriendApplyRecordMapper;
import com.developer.friend.pojo.FriendApplyRecordPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FriendApplyRecordPORepository extends ServiceImpl<FriendApplyRecordMapper, FriendApplyRecordPO> {

    public FriendApplyRecordPO findRecord(Long targetUserId, Long mainUserId){
        return this.lambdaQuery().eq(FriendApplyRecordPO::getTargetUserId,targetUserId).eq(FriendApplyRecordPO::getMainUserId,mainUserId).one();
    }

    public void updateStatus(Long targetUserId,Long mainUserId,Integer status){
        this.lambdaUpdate().eq(FriendApplyRecordPO::getTargetUserId,targetUserId).eq(FriendApplyRecordPO::getMainUserId,mainUserId).set(FriendApplyRecordPO::getStatus,status).update();
    }

    public List<FriendApplyRecordPO> findRecordByStatus(Long targetUserId, AddFriendStatusEnum status){
        return this.lambdaQuery().eq(FriendApplyRecordPO::getTargetUserId,targetUserId).eq(FriendApplyRecordPO::getStatus,status.code()).list();
    }

    public boolean updateStatusSentToViewed(Long targetUserId){
        return this.lambdaUpdate().eq(FriendApplyRecordPO::getTargetUserId, targetUserId).eq(FriendApplyRecordPO::getStatus, AddFriendStatusEnum.SENT.code()).set(FriendApplyRecordPO::getStatus, AddFriendStatusEnum.VIEWED.code()).update();
    }
}
