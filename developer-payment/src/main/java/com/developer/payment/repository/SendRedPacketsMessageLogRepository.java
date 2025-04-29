package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.SendRedPacketsMessageLogMapper;
import com.developer.payment.pojo.SendRedPacketsMessageLogPO;
import org.springframework.stereotype.Repository;

@Repository
public class SendRedPacketsMessageLogRepository extends ServiceImpl<SendRedPacketsMessageLogMapper, SendRedPacketsMessageLogPO> {

    public SendRedPacketsMessageLogPO findBySerialNo(String serialNo){
        return this.lambdaQuery().eq(SendRedPacketsMessageLogPO::getSerialNo,serialNo).one();
    }

}
