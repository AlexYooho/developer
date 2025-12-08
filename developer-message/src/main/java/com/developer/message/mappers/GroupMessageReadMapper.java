package com.developer.message.mappers;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.message.pojo.GroupMessageReadPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMessageReadMapper extends BaseMapper<GroupMessageReadPO> {

    List<GroupMessageReadPO> findUserReadGroupMessageList(@Param("group_id")Long groupId,@Param("user_id")Long userId);

}
