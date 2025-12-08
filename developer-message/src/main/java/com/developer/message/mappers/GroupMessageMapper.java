package com.developer.message.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.message.pojo.GroupMessagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessagePO> {

    List<GroupMessagePO> findMessageList(@Param("group_id")Long groupId,@Param("last_seq")Long lastSeq);

    void updateMessageReadCount(@Param("group_id")Long groupId,@Param("msg_ids")List<Long> msgIds);

}
