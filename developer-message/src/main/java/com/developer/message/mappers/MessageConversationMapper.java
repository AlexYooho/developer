package com.developer.message.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.message.pojo.MessageConversationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageConversationMapper extends BaseMapper<MessageConversationPO> {

    List<MessageConversationPO> findList(@Param("user_id") Long userId);

    void updateConversationInfo(@Param("po") MessageConversationPO po);

}
