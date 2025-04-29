package com.developer.payment.listener.processor;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.SendRedPacketsMessageLogPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.SendRedPacketsMessageLogRepository;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedPacketsMessageCheckProcessor implements IMessageProcessor {

    @Autowired
    private SendRedPacketsMessageLogRepository sendRedPacketsMessageLogRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.RED_PACKETS_MESSAGE_SEND_CHECK;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        SendRedPacketsMessageLogPO log = sendRedPacketsMessageLogRepository.findBySerialNo(dto.serialNo);
        if(log==null){
            return DeveloperResult.error(dto.serialNo,"未找到发送记录");
        }

        if(log.getSendStatus()==1){
            return DeveloperResult.success(dto.serialNo);
        }

        // 未发送成功,做逻辑补偿,回滚红包数据以及钱包余额
        RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(dto.parseData(Long.class));
        DeveloperResult<Boolean> operationWalletResult = walletService.doMoneyTransaction(dto.serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), redPacketsInfoPO.getSendAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
        if(!operationWalletResult.getIsSuccessful()){
            return DeveloperResult.error(dto.serialNo,"钱包余额回滚异常");
        }

        redPacketsInfoPO.setStatus(RedPacketsStatusEnum.SEND_FAILURE);
        redPacketsInfoRepository.updateById(redPacketsInfoPO);

        log.setSendStatus(2);
        sendRedPacketsMessageLogRepository.updateById(log);

        return DeveloperResult.success(dto.serialNo);
    }
}
