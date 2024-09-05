package com.developer.group.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.group.mappers.GroupMemberMapper;
import com.developer.group.pojo.GroupMemberPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupMemberRepository extends ServiceImpl<GroupMemberMapper, GroupMemberPO> {

    public GroupMemberPO findByGroupIdAndUserId(Long groupId,Long userId){
        return this.lambdaQuery().eq(GroupMemberPO::getGroupId,groupId).eq(GroupMemberPO::getUserId,userId).one();
    }

    public void removeByGroupId(Long groupId){
        this.lambdaUpdate().eq(GroupMemberPO::getGroupId,groupId)
                .set(GroupMemberPO::getQuit,true).update();
    }

    public List<GroupMemberPO> findByUserId(Long userId){
        return this.lambdaQuery().eq(GroupMemberPO::getUserId,userId)
                .eq(GroupMemberPO::getQuit,false)
                .list();
    }

    public List<GroupMemberPO> findByGroupId(Long groupId){
        return this.lambdaQuery().eq(GroupMemberPO::getGroupId,groupId).eq(GroupMemberPO::getQuit,0).list();
    }

    public void removeByGroupAndUserId(Long groupId,Long userId){
        this.lambdaUpdate().eq(GroupMemberPO::getGroupId,groupId)
                .eq(GroupMemberPO::getUserId,userId)
                .set(GroupMemberPO::getQuit,true)
                .update();
    }
}
