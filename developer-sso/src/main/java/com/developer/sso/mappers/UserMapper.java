package com.developer.sso.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.sso.pojo.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
