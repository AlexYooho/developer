package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.GroupMessageMapper;
import com.developer.message.pojo.GroupMessagePO;
import org.springframework.stereotype.Repository;

@Repository
public class GroupMessageRepository extends ServiceImpl<GroupMessageMapper, GroupMessagePO> {
}
