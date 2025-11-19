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

    public List<GroupInfoPO> findByGroupInfo(List<Long> groupIds){
        return this.lambdaQuery().in(GroupInfoPO::getId,groupIds).eq(GroupInfoPO::getDeleted,false).list();
    }
}
