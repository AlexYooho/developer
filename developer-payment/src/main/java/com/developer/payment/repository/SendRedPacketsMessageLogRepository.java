package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.SendRedPacketsMessageLogMapper;
import com.developer.payment.pojo.SendPaymentMessageLogPO;
import org.springframework.stereotype.Repository;

@Repository
public class SendRedPacketsMessageLogRepository extends ServiceImpl<SendRedPacketsMessageLogMapper, SendPaymentMessageLogPO> {

    public SendPaymentMessageLogPO findBySerialNo(String serialNo){
        return this.lambdaQuery().eq(SendPaymentMessageLogPO::getSerialNo,serialNo).one();
    }

}
