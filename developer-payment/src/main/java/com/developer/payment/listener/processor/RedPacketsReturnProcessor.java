package com.developer.payment.listener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RedPacketsReturnProcessor implements IMessageProcessor {
    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.RED_PACKETS_RETURN;
    }

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private WalletService walletService;

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        Long redPacketsId = dto.parseData(Long.class);
        RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(redPacketsId);
        if(redPacketsInfoPO==null){
            return DeveloperResult.error(dto.getSerialNo(),"红包不存在");
        }

        if(redPacketsInfoPO.getStatus() == RedPacketsStatusEnum.FINISHED || redPacketsInfoPO.getStatus() == RedPacketsStatusEnum.REFUND){
            return DeveloperResult.error(dto.getSerialNo(),"红包状态不可操作");
        }

        if(redPacketsInfoPO.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0){
            return DeveloperResult.error(dto.getSerialNo(),"红包剩余金额为0,无需退回");
        }

        redPacketsInfoPO.setReturnAmount(redPacketsInfoPO.getRemainingAmount());
        redPacketsInfoPO.setStatus(RedPacketsStatusEnum.REFUND);
        redPacketsInfoPO.setRemainingCount(0);
        redPacketsInfoPO.setRemainingAmount(BigDecimal.ZERO);
        redPacketsInfoRepository.updateById(redPacketsInfoPO);

        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(dto.getSerialNo(), redPacketsInfoPO.getSenderUserId(), redPacketsInfoPO.getRemainingAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
        if (!walletResult.getIsSuccessful()) {
            return DeveloperResult.error(walletResult.getSerialNo(), walletResult.getMsg());
        }

        return DeveloperResult.success(dto.getSerialNo());
    }
}
