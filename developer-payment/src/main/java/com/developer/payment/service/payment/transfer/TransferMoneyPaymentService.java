package com.developer.payment.service.payment.transfer;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.PaymentChannelEnum;
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

    /**
     * 发起转账
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto) {

        if(dto.getTransferInfoDTO().getTransferAmount().compareTo(BigDecimal.ZERO)<=0){
            return DeveloperResult.error("转账金额必须大于0");
        }


        if(dto.getTransferInfoDTO().getPaymentChannel()== PaymentChannelEnum.FRIEND && dto.getTransferInfoDTO().getToUserId()==null){
            return DeveloperResult.error("请指定转账对象");
        }

        if(dto.getTransferInfoDTO().getPaymentChannel()== PaymentChannelEnum.GROUP && (dto.getTransferInfoDTO().getToGroupId()==null || dto.getTransferInfoDTO().getToUserId()==null)) {
            return DeveloperResult.error("请指定转账群组和对象");
        }

        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(dto.getTransferInfoDTO().getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.EXPENDITURE);
        if(!transactionResult.getIsSuccessful()){
            return DeveloperResult.error(transactionResult.getMsg());
        }

        transferInfoRepository.save(TransferInfoPO.builder().TransferAmount(dto.getTransferInfoDTO().getTransferAmount()).userId(SelfUserInfoContext.selfUserInfo().getUserId())
                .receiverUserId(dto.getTransferInfoDTO().getToUserId()).transferStatus(TransferStatusEnum.PENDING)
                .createdTime(new Date()).updateTime(new Date()).build());

        return DeveloperResult.success();
    }

    /**
     * 确认收款
     * @param id
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> confirmReceipt(Long id) {
        return null;
    }

    /**
     * 退回金额
     * @param id
     * @return
     */
    @Override
    public DeveloperResult<Boolean> amountRefunded(Long id) {
        return null;
    }
}
