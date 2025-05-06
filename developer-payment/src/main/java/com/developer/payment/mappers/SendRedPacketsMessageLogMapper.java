package com.developer.payment.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.developer.payment.pojo.SendPaymentMessageLogPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SendRedPacketsMessageLogMapper extends BaseMapper<SendPaymentMessageLogPO> {
}
