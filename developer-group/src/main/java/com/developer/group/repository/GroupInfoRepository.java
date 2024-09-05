package com.developer.group.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.group.mappers.GroupInfoMapper;
import com.developer.group.pojo.GroupInfoPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupInfoRepository extends ServiceImpl<GroupInfoMapper, GroupInfoPO> {

    public List<GroupInfoPO> findByGroupIds(List<Long> groupIds){
        return this.lambdaQuery().in(GroupInfoPO::getId,groupIds).list();
    }

}
