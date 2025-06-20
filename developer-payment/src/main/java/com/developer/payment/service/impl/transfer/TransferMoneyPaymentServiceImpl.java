package com.developer.payment.service.impl.transfer;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.exception.DeveloperBusinessException;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.TransferStatusEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.TransferInfoPO;
import com.developer.payment.repository.TransferInfoRepository;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.impl.BasePaymentService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Service
public class TransferMoneyPaymentServiceImpl extends BasePaymentService implements PaymentService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransferInfoRepository transferInfoRepository;


    @Override
    public PaymentTypeEnum paymentType() {
        return PaymentTypeEnum.TRANSFER;
    }

    /**
     * 发起转账
     *
     * @param dto
     * @return
     */
    @Override
    @GlobalTransactional(name = "transfer-transaction-tx", rollbackFor = Exception.class)
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();

        // 1、转账条件判断
        DeveloperResult<Boolean> judgmentResult = paymentCommentConditionalJudgment(dto.getTransferInfoDTO().getSerialNo(), PaymentTypeEnum.TRANSFER, dto.getTransferInfoDTO().getPaymentChannel(), null, dto.getTransferInfoDTO().getTransferAmount(), dto.getTransferInfoDTO().getTargetId(), SelfUserInfoContext.selfUserInfo().getUserId(), 0);
        if (!judgmentResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo, judgmentResult.getMsg());
        }

        if (dto.getTransferInfoDTO().getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DeveloperBusinessException(serialNo, "转账金额必须大于0");
        }

        // 2、转账信息入库
        TransferInfoPO transferInfoPO = TransferInfoPO.builder()
                .transferAmount(dto.getTransferInfoDTO().getTransferAmount())
                .userId(SelfUserInfoContext.selfUserInfo().getUserId())
                .receiverUserId(dto.getTransferInfoDTO().getTargetId())
                .transferStatus(TransferStatusEnum.PENDING)
                .transferTime(new Date())
                .expireTime(DateTimeUtils.addTime(24, ChronoUnit.HOURS))
                .createdTime(new Date())
                .updateTime(new Date())
                .build();
        transferInfoRepository.save(transferInfoPO);

        // 3、处理钱包信息
        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), dto.getTransferInfoDTO().getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.EXPENDITURE);
        if (!transactionResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo, transactionResult.getMsg());
        }

        // 4、推送转账消息
        String messageContent = "你收到一笔转账";
        DeveloperResult sendMessageResult = sendRedPacketsMessage(serialNo,dto.getTransferInfoDTO().getTargetId(), dto.getTransferInfoDTO().getPaymentChannel(),transferInfoPO.getId(),PaymentTypeEnum.TRANSFER,messageContent, MessageContentTypeEnum.TRANSFER);
        if (!sendMessageResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,sendMessageResult.getMsg());
        }

        // 5、推送转账过期延迟检查事件
        transactionExpiredCheckEvent(serialNo,PaymentTypeEnum.TRANSFER,transferInfoPO.getId(),transferInfoPO.getExpireTime().getTime());

        return DeveloperResult.success(serialNo);
    }

    /**
     * 确认收款
     *
     * @param req
     * @return
     */
    @Override
    @GlobalTransactional(name = "transfer-confirm-transaction-tx", rollbackFor = Exception.class)
    public DeveloperResult<BigDecimal> amountCharged(OpenRedPacketsRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        TransferInfoPO transferInfo = transferInfoRepository.getById(req.getRedPacketsId());
        if (transferInfo == null) {
            return DeveloperResult.error(serialNo, "转账记录不存在");
        }

        if (!Objects.equals(transferInfo.getReceiverUserId(), SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(serialNo, "您不是收款人，无法确认收款");
        }

        if (transferInfo.getTransferStatus() == TransferStatusEnum.SUCCESS) {
            return DeveloperResult.error(serialNo, "已确认收款,无法再次收款");
        }

        transferInfo.setTransferStatus(TransferStatusEnum.SUCCESS);
        transferInfo.setUpdateTime(new Date());
        transferInfoRepository.updateById(transferInfo);

        // 增加钱包余额
        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), transferInfo.getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.INCOME);
        if (!transactionResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,transactionResult.getMsg());
        }

        return DeveloperResult.success(serialNo, transferInfo.getTransferAmount());
    }

    /**
     * 退回金额
     *
     * @param req
     * @return
     */
    @Override
    @GlobalTransactional(name = "transfer-return-transaction-tx", rollbackFor = Exception.class)
    public DeveloperResult<Boolean> amountRefunded(ReturnTransferRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        TransferInfoPO transferInfo = transferInfoRepository.getById(req.getRedPacketsId());
        if (transferInfo == null) {
            return DeveloperResult.error(serialNo, "转账记录不存在");
        }

        if (transferInfo.getTransferStatus() == TransferStatusEnum.SUCCESS || transferInfo.getTransferStatus() == TransferStatusEnum.REFUND) {
            return DeveloperResult.error(serialNo, "转账已被收款或已退回,无法操作");
        }

        transferInfo.setTransferStatus(TransferStatusEnum.REFUND);
        transferInfo.setUpdateTime(new Date());
        transferInfoRepository.updateById(transferInfo);

        // 增加钱包余额
        DeveloperResult<Boolean> transactionResult = walletService.doMoneyTransaction(serialNo, transferInfo.getUserId(), transferInfo.getTransferAmount(), TransactionTypeEnum.TRANSFER, WalletOperationTypeEnum.INCOME);
        if (!transactionResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,transactionResult.getMsg());
        }

        return DeveloperResult.success(serialNo);
    }
}
