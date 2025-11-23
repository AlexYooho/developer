package com.developer.message.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.message.mappers.MessageConversationMapper;
import com.developer.message.pojo.MessageConversationPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageConversationRepository extends ServiceImpl<MessageConversationMapper, MessageConversationPO> {

    public List<MessageConversationPO> findList(Long userId){
        return baseMapper.findList(userId);
    }

}
