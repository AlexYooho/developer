package com.developer.group.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.group.dto.SelfJoinGroupInfoDTO;
import com.developer.group.mappers.GroupInfoMapper;
import com.developer.group.pojo.GroupInfoPO;
import com.developer.group.pojo.GroupMemberPO;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class GroupInfoRepository extends ServiceImpl<GroupInfoMapper, GroupInfoPO> {

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    public List<GroupInfoPO> findByGroupIds(List<Long> groupIds){
        return this.lambdaQuery().in(GroupInfoPO::getId,groupIds).list();
    }

    public List<SelfJoinGroupInfoDTO> findUserJoinGroupInfo(Long userId){
        MPJLambdaWrapper<GroupInfoPO> lambdaWrapper = JoinWrappers.lambda(GroupInfoPO.class)
                .selectAs(GroupInfoPO::getId,SelfJoinGroupInfoDTO::getGroupId)
                .selectAs(GroupInfoPO::getName,SelfJoinGroupInfoDTO::getGroupName)
                .selectAs(GroupInfoPO::getHeadImage,SelfJoinGroupInfoDTO::getGroupName)
                .selectAs(GroupMemberPO::getUserId,SelfJoinGroupInfoDTO::getUserId)
                .selectAs(GroupMemberPO::getQuit,SelfJoinGroupInfoDTO::getQuit)
                .selectAs(GroupMemberPO::getAliasName,SelfJoinGroupInfoDTO::getAliasName)
                .selectAs(GroupMemberPO::getCreatedTime,SelfJoinGroupInfoDTO::getCreatedTime)
                .selectAs(GroupMemberPO::getHeadImage,SelfJoinGroupInfoDTO::getHeadImage)
                .innerJoin(GroupMemberPO.class,GroupMemberPO::getGroupId,GroupInfoPO::getId)
                .eq(GroupMemberPO::getUserId,userId);
        return groupInfoMapper.selectJoinList(SelfJoinGroupInfoDTO.class,lambdaWrapper);
    }

}
