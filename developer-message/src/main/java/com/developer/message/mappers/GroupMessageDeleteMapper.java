package com.developer.message.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.message.pojo.GroupMessageDeletePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMessageDeleteMapper extends BaseMapper<GroupMessageDeletePO> {

    List<GroupMessageDeletePO> findMessages(@Param("group_id")Long groupId,@Param("user_id")Long userId);

}
