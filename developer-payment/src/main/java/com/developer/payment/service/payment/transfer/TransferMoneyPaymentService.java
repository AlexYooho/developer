package com.developer.payment.service.payment.transfer;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.TransferStatusEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.TransferInfoPO;
import com.developer.payment.repository.TransferInfoRepository;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class TransferMoneyPaymentService implements PaymentService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransferInfoRepository transferInfoRepository;

    @Override
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto) {

        if(dto.getTransferInfoDTO().getTransferAmount().compareTo(BigDecimal.ZERO)<=0){
            return DeveloperResult.error("转账金额必须大于0");
        }

        if(dto.getTransferInfoDTO().getTargetId()<=0){
            return DeveloperResult.error("请指定转账对象");
        }

        walletService.doMoneyTransaction(dto.getTransferInfoDTO().getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.EXPENDITURE);

        TransferInfoPO transferInfoPO = TransferInfoPO.builder().TransferAmount(dto.getTransferInfoDTO().getTransferAmount()).userId(SelfUserInfoContext.selfUserInfo().getUserId())
                .receiverUserId(dto.getTransferInfoDTO().getTargetId()).transferStatus(TransferStatusEnum.PENDING)
                .createdTime(new Date()).updateTime(new Date()).build();
        transferInfoRepository.save(transferInfoPO);

        return DeveloperResult.success();
    }
}
