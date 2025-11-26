package com.developer.message.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.message.pojo.PrivateMessagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessagePO> {

    List<PrivateMessagePO> findMessageList(@Param("min_id") Long minId, @Param("uid_a") Long uidA, @Param("uid_b") Long uidB);

    void modifyMessageStatus(@Param("status")MessageStatusEnum status,@Param("ids") List<Long> ids);

}
