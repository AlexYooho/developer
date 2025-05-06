package com.developer.payment.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.pojo.SendPaymentMessageLogPO;
import com.developer.payment.repository.SendRedPacketsMessageLogRepository;
import com.developer.payment.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SendRedPacketsMessageLogRepository sendRedPacketsMessageLogRepository;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public DeveloperResult<Boolean> modifyRedPacketMessageStatus(String serialNo, Integer sendStatus) {
        serialNo = snowflakeNoUtil.getSerialNo(serialNo);
        SendPaymentMessageLogPO log = sendRedPacketsMessageLogRepository.findBySerialNo(serialNo);
        if(log==null){
            return DeveloperResult.error(serialNo,"记录不存在");
        }

        log.setSendStatus(sendStatus);
        sendRedPacketsMessageLogRepository.updateById(log);
        return DeveloperResult.success(serialNo);
    }
}
