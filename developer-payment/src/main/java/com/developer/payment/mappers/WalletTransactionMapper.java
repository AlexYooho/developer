package com.developer.payment.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.payment.pojo.WalletTransactionPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransactionPO> {
}
