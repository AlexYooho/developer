package com.developer.payment.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.payment.pojo.WalletTransactionRecordPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTransactionRecordMapper extends BaseMapper<WalletTransactionRecordPO> {
}
