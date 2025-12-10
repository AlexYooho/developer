package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.GroupMessageDeleteMapper;
import com.developer.message.pojo.GroupMessageDeletePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupMessageDeleteRepository extends ServiceImpl<GroupMessageDeleteMapper, GroupMessageDeletePO> {

    public List<GroupMessageDeletePO> findMessages(Long groupId,Long userId){
        return baseMapper.findMessages(groupId,userId);
    }

}
