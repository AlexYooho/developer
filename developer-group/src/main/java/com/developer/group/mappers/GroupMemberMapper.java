package com.developer.group.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.framework.model.DeveloperResult;
import com.developer.group.pojo.GroupMemberPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMemberPO> {

    List<GroupMemberPO> findGroupByMember(@Param("group_ids") List<Long> groupIds,@Param("user_id") Long userId);

}
