package com.developer.payment.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.enums.TransactionStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletStatusEnum;
import com.developer.payment.pojo.UserWalletPO;
import com.developer.payment.pojo.WalletTransactionPO;
import com.developer.payment.repository.UserWalletRepository;
import com.developer.payment.repository.WalletTransactionRepository;
import com.developer.payment.service.WalletService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserWalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Override
    @TwoPhaseBusinessAction(name = "doMoneyTransaction", commitMethod = "confirmTransaction", rollbackMethod = "cancelTransaction")
    public DeveloperResult<Boolean> doMoneyTransaction(BusinessActionContext context,Long senderId, Long targetId, BigDecimal amount) {

        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if(walletInfo==null){
            return DeveloperResult.error("用户未开通钱包");
        }

        if (walletInfo.getBalance().compareTo(amount) < 0) {
            return DeveloperResult.error("余额不足");
        }

        if(walletInfo.getStatus() == WalletStatusEnum.FROZEN){
            return DeveloperResult.error("钱包被冻结");
        }

        BigDecimal beforeBalance = walletInfo.getBalance();
        BigDecimal afterBalance = walletInfo.getBalance().subtract(amount);

        walletInfo.setLastTransactionTime(new Date());
        walletInfo.setUpdateTime(new Date());
        walletInfo.setBalance(walletInfo.getBalance().subtract(amount));
        walletRepository.updateById(walletInfo);

        walletTransactionRepository.save(WalletTransactionPO.builder().walletId(walletInfo.getId()).userId(userId).transactionType(TransactionTypeEnum.TRANSFER).amount(amount).beforeBalance(beforeBalance).afterBalance(afterBalance)
                .relatedUserId(targetId).referenceId(context.getXid()).status(TransactionStatusEnum.PENDING).createdTime(new Date()).updateTime(new Date()).build());

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult<Boolean> confirmTransaction(BusinessActionContext context) {
        // Confirm阶段：确认支付并更新交易状态
        String xid = context.getXid();
        WalletTransactionPO transaction = walletTransactionRepository.findByReferenceId(xid);
        if (transaction != null && transaction.getStatus() == TransactionStatusEnum.PENDING) {
            transaction.setStatus(TransactionStatusEnum.SUCCESS);
            transaction.setUpdateTime(new Date());
            walletTransactionRepository.updateById(transaction);
            return DeveloperResult.success();
        }
        return DeveloperResult.error("交易确认失败");
    }

    @Override
    public DeveloperResult<Boolean> cancelTransaction(BusinessActionContext context) {
        // Cancel阶段：撤销交易，解冻金额
        String xid = context.getXid();
        WalletTransactionPO transaction = walletTransactionRepository.findByReferenceId(xid);
        if (transaction != null && transaction.getStatus() == TransactionStatusEnum.PENDING) {
            // 还原余额
            UserWalletPO walletInfo = walletRepository.getById(transaction.getWalletId());
            walletInfo.setBalance(walletInfo.getBalance().add(transaction.getAmount()));
            walletInfo.setUpdateTime(new Date());
            walletRepository.updateById(walletInfo);

            transaction.setStatus(TransactionStatusEnum.FAILED);
            transaction.setUpdateTime(new Date());
            walletTransactionRepository.updateById(transaction);
            return DeveloperResult.success();
        }
        return DeveloperResult.error("交易撤销失败");
    }
}
