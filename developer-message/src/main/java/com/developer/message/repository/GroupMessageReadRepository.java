package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.GroupMessageReadMapper;
import com.developer.message.pojo.GroupMessageReadPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupMessageReadRepository extends ServiceImpl<GroupMessageReadMapper, GroupMessageReadPO> {

    public List<GroupMessageReadPO> findUserReadGroupMessageList(Long groupId,Long userId){
        return baseMapper.findUserReadGroupMessageList(groupId,userId);
    }

}
