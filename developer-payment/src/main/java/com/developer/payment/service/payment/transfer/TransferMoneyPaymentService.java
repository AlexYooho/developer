package com.developer.payment.service.payment.transfer;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;
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
import java.util.Objects;

@Service
public class TransferMoneyPaymentService implements PaymentService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransferInfoRepository transferInfoRepository;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    /**
     * 发起转账
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto) {
        String serialNo = dto.getTransferInfoDTO().getSerialNo().isEmpty() ? snowflakeNoUtil.getSerialNo() : dto.getTransferInfoDTO().getSerialNo();
        if(dto.getTransferInfoDTO().getTransferAmount().compareTo(BigDecimal.ZERO)<=0){
            return DeveloperResult.error(serialNo,"转账金额必须大于0");
        }

        if(dto.getTransferInfoDTO().getPaymentChannel()== PaymentChannelEnum.FRIEND && dto.getTransferInfoDTO().getToUserId()==null){
            return DeveloperResult.error(serialNo,"请指定转账对象");
        }

        if(dto.getTransferInfoDTO().getPaymentChannel()== PaymentChannelEnum.GROUP && (dto.getTransferInfoDTO().getToGroupId()==null || dto.getTransferInfoDTO().getToUserId()==null)) {
            return DeveloperResult.error(serialNo,"请指定转账群组和对象");
        }

        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo,SelfUserInfoContext.selfUserInfo().getUserId(), dto.getTransferInfoDTO().getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.EXPENDITURE);
        if(!transactionResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,transactionResult.getMsg());
        }

        transferInfoRepository.save(TransferInfoPO.builder().TransferAmount(dto.getTransferInfoDTO().getTransferAmount()).userId(SelfUserInfoContext.selfUserInfo().getUserId())
                .receiverUserId(dto.getTransferInfoDTO().getToUserId()).transferStatus(TransferStatusEnum.PENDING)
                .createdTime(new Date()).updateTime(new Date()).build());

        return DeveloperResult.success(serialNo);
    }

    /**
     * 确认收款
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> amountCharged(OpenRedPacketsRequestDTO req) {
        String serialNo = req.getSerialNo().isEmpty() ? snowflakeNoUtil.getSerialNo() : req.getSerialNo();
        TransferInfoPO transferInfo = transferInfoRepository.getById(req.getRedPacketsId());
        if(transferInfo==null){
            return DeveloperResult.error(serialNo,"转账记录不存在");
        }

        if(!Objects.equals(transferInfo.getReceiverUserId(), SelfUserInfoContext.selfUserInfo().getUserId())){
            return DeveloperResult.error(serialNo,"您不是收款人，无法确认收款");
        }

        if(transferInfo.getTransferStatus() == TransferStatusEnum.SUCCESS){
            return DeveloperResult.error(serialNo,"已确认收款,无法再次收款");
        }

        transferInfo.setTransferStatus(TransferStatusEnum.SUCCESS);
        transferInfo.setUpdateTime(new Date());
        transferInfoRepository.updateById(transferInfo);

        // 增加钱包余额
        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo,SelfUserInfoContext.selfUserInfo().getUserId(), transferInfo.getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.INCOME);
        if(!transactionResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,transactionResult.getMsg());
        }

        return DeveloperResult.success(serialNo,transferInfo.getTransferAmount());
    }

    /**
     * 退回金额
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> amountRefunded(ReturnTransferRequestDTO req) {
        String serialNo = req.getSerialNo().isEmpty() ? snowflakeNoUtil.getSerialNo() : req.getSerialNo();
        TransferInfoPO transferInfo = transferInfoRepository.getById(req.getRedPacketsId());
        if(transferInfo==null){
            return DeveloperResult.error(serialNo,"转账记录不存在");
        }

        if(transferInfo.getTransferStatus() == TransferStatusEnum.SUCCESS || transferInfo.getTransferStatus() == TransferStatusEnum.REFUND){
            return DeveloperResult.error(serialNo,"转账已被收款或已退回,无法操作");
        }

        transferInfo.setTransferStatus(TransferStatusEnum.REFUND);
        transferInfo.setUpdateTime(new Date());
        transferInfoRepository.updateById(transferInfo);

        // 增加钱包余额
        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo,transferInfo.getUserId(), transferInfo.getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.INCOME);
        if(!transactionResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,transactionResult.getMsg());
        }

        return DeveloperResult.success(serialNo);
    }
}
