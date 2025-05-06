package com.developer.payment.listener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.payment.dto.TransactionExpiredCheckDTO;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.TransferStatusEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.TransferInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.TransferInfoRepository;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class TransactionExpiredCheckProcessor implements IMessageProcessor {
    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.TRANSACTION_EXPIRED_CHECK;
    }

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransferInfoRepository transferInfoRepository;

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        TransactionExpiredCheckDTO transactionExpiredCheckDTO = dto.parseData(TransactionExpiredCheckDTO.class);
        Long userId = 0L;
        BigDecimal remainingAmount = BigDecimal.ZERO;
        if (transactionExpiredCheckDTO.getPaymentTypeEnum() == PaymentTypeEnum.RED_PACKETS) {
            RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(transactionExpiredCheckDTO.getTransactionId());
            if (redPacketsInfoPO == null) {
                return DeveloperResult.error(dto.getSerialNo(), "红包不存在");
            }

            if (redPacketsInfoPO.getStatus() == RedPacketsStatusEnum.FINISHED || redPacketsInfoPO.getStatus() == RedPacketsStatusEnum.REFUND) {
                return DeveloperResult.error(dto.getSerialNo(), "红包状态不可操作");
            }

            if (redPacketsInfoPO.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return DeveloperResult.error(dto.getSerialNo(), "红包剩余金额为0,无需退回");
            }

            redPacketsInfoPO.setReturnAmount(redPacketsInfoPO.getRemainingAmount());
            redPacketsInfoPO.setStatus(RedPacketsStatusEnum.REFUND);
            redPacketsInfoPO.setRemainingCount(0);
            redPacketsInfoPO.setRemainingAmount(BigDecimal.ZERO);
            redPacketsInfoRepository.updateById(redPacketsInfoPO);

            userId = redPacketsInfoPO.getSenderUserId();
            remainingAmount = redPacketsInfoPO.getRemainingAmount();
        }

        if(transactionExpiredCheckDTO.getPaymentTypeEnum() == PaymentTypeEnum.TRANSFER){
            TransferInfoPO transferInfoPO = transferInfoRepository.getById(transactionExpiredCheckDTO.getTransactionId());
            if(transferInfoPO == null){
                return DeveloperResult.error(dto.getSerialNo(),"转账信息不存在");
            }

            if(transferInfoPO.getTransferStatus() != TransferStatusEnum.PENDING){
                return DeveloperResult.error(dto.getSerialNo(),"转账状态不可操作");
            }

            transferInfoPO.setTransferStatus(TransferStatusEnum.REFUND);
            transferInfoPO.setReturnTime(new Date());
            transferInfoPO.setReturnAmount(transferInfoPO.getTransferAmount());
            transferInfoPO.setUpdateTime(new Date());
            transferInfoRepository.updateById(transferInfoPO);

            userId = transferInfoPO.getUserId();
            remainingAmount = transferInfoPO.getTransferAmount();
        }

        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(dto.getSerialNo(), userId, remainingAmount, TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
        if (!walletResult.getIsSuccessful()) {
            return DeveloperResult.error(walletResult.getSerialNo(), walletResult.getMsg());
        }

        return DeveloperResult.success(dto.getSerialNo());
    }
}
