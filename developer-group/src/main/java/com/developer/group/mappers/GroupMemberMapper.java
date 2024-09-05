package com.developer.group.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.group.pojo.GroupMemberPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMemberPO> {
}
