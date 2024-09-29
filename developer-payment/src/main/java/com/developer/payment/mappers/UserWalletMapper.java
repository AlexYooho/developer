package com.developer.payment.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.payment.pojo.UserWalletPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWalletPO> {
}
