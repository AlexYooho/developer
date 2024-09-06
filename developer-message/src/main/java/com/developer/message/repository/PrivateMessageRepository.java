package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.PrivateMessageMapper;
import com.developer.message.pojo.PrivateMessagePO;
import org.springframework.stereotype.Repository;

@Repository
public class PrivateMessageRepository extends ServiceImpl<PrivateMessageMapper, PrivateMessagePO> {
}
