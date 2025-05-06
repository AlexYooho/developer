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
import com.developer.payment.pojo.SendPaymentMessageLogPO;
import com.developer.payment.pojo.TransferInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.SendRedPacketsMessageLogRepository;
import com.developer.payment.repository.TransferInfoRepository;
import com.developer.payment.service.WalletService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class TransactionMessageCheckProcessor implements IMessageProcessor {

    @Autowired
    private SendRedPacketsMessageLogRepository sendRedPacketsMessageLogRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private TransferInfoRepository transferInfoRepository;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.TRANSACTION_MESSAGE_SEND_CHECK;
    }

    @Override
    @GlobalTransactional(name = "transaction-message-send-check-tx", rollbackFor = Exception.class)
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        SendPaymentMessageLogPO log = sendRedPacketsMessageLogRepository.findBySerialNo(dto.serialNo);
        if(log==null){
            return DeveloperResult.error(dto.serialNo,"未找到发送记录");
        }

        if(log.getSendStatus()==1){
            return DeveloperResult.success(dto.serialNo);
        }

        TransactionExpiredCheckDTO transactionExpiredCheckDTO = dto.parseData(TransactionExpiredCheckDTO.class);
        Long userId = 0L;
        BigDecimal amount = BigDecimal.ZERO;

        // 未发送成功,做逻辑补偿,回滚交易数据以及钱包余额
        if(transactionExpiredCheckDTO.getPaymentTypeEnum() == PaymentTypeEnum.RED_PACKETS){
            RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(transactionExpiredCheckDTO.getTransactionId());
            userId = redPacketsInfoPO.getSenderUserId();
            amount = redPacketsInfoPO.getRemainingAmount();
            redPacketsInfoPO.setRemainingCount(0);
            redPacketsInfoPO.setStatus(RedPacketsStatusEnum.SEND_FAILURE);
            redPacketsInfoPO.setRemainingAmount(BigDecimal.ZERO);
            redPacketsInfoPO.setReturnAmount(amount);
            redPacketsInfoPO.setUpdateTime(new Date());
            redPacketsInfoRepository.updateById(redPacketsInfoPO);
        }else{
            TransferInfoPO transferInfoPO = transferInfoRepository.getById(transactionExpiredCheckDTO.getTransactionId());
            userId = transferInfoPO.getUserId();
            amount = transferInfoPO.getTransferAmount();
            transferInfoPO.setTransferStatus(TransferStatusEnum.FAILED);
            transferInfoPO.setReturnTime(new Date());
            transferInfoPO.setReturnAmount(amount);
            transferInfoPO.setUpdateTime(new Date());
            transferInfoRepository.updateById(transferInfoPO);
        }

        DeveloperResult<Boolean> operationWalletResult = walletService.doMoneyTransaction(dto.serialNo, userId, amount, TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
        if(!operationWalletResult.getIsSuccessful()){
            return DeveloperResult.error(dto.serialNo,"钱包余额回滚异常");
        }

        log.setSendStatus(2);
        sendRedPacketsMessageLogRepository.updateById(log);

        // 推送消息--红包退回

        return DeveloperResult.success(dto.serialNo);
    }
}
