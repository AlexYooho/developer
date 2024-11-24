package com.developer.payment.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.CurrencyEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.enums.TransactionStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.enums.WalletStatusEnum;
import com.developer.payment.pojo.UserWalletPO;
import com.developer.payment.pojo.WalletTransactionRecordPO;
import com.developer.payment.repository.UserWalletRepository;
import com.developer.payment.repository.WalletTransactionRecordRepository;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserWalletRepository walletRepository;

    @Autowired
    private WalletTransactionRecordRepository walletTransactionRepository;

    /**
     * 发起交易
     * @param amount
     * @return
     */
    @Override
    public DeveloperResult<Boolean> doMoneyTransaction(Long userId,BigDecimal amount,TransactionTypeEnum transactionType, WalletOperationTypeEnum operationType) {
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if(walletInfo==null){
            return DeveloperResult.error("用户未开通钱包");
        }

        if (operationType==WalletOperationTypeEnum.EXPENDITURE && walletInfo.getBalance().compareTo(amount) < 0) {
            return DeveloperResult.error("余额不足");
        }

        if(walletInfo.getStatus() == WalletStatusEnum.FROZEN){
            return DeveloperResult.error("钱包被冻结");
        }

        BigDecimal beforeBalance = walletInfo.getBalance();
        BigDecimal afterBalance = operationType==WalletOperationTypeEnum.EXPENDITURE ?
                walletInfo.getBalance().subtract(amount.abs()) : walletInfo.getBalance().add(amount.abs());

        walletInfo.setLastTransactionTime(new Date());
        walletInfo.setUpdateTime(new Date());
        walletInfo.setBalance(afterBalance);
        walletRepository.updateById(walletInfo);

        walletTransactionRepository.save(WalletTransactionRecordPO.builder()
                .walletId(walletInfo.getId())
                .userId(userId)
                .transactionType(transactionType)
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .relatedUserId(userId)
                .referenceId("")
                .status(TransactionStatusEnum.PENDING)
                .createdTime(new Date())
                .updateTime(new Date())
                .build());

        return DeveloperResult.success();
    }

    /**
     * 冻结支付金额
     * @param amount
     * @return
     */
    @Override
    public DeveloperResult<Boolean> freezePaymentAmount(BigDecimal amount) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);

        if (walletInfo.getBalance().compareTo(amount) < 0) {
            return DeveloperResult.error("余额不足");
        }

        walletInfo.setFrozenBalance(walletInfo.getFrozenBalance().add(amount));
        walletInfo.setUpdateTime(new Date());
        walletRepository.updateById(walletInfo);

        return DeveloperResult.success();
    }

    /**
     * 创建钱包
     * @return
     */
    @Override
    public DeveloperResult<Boolean> CreateWallet() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if(walletInfo!=null){
            return DeveloperResult.error("用户已开通钱包");
        }

        walletRepository.save(UserWalletPO.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .totalRecharge(BigDecimal.ZERO)
                .totalWithdraw(BigDecimal.ZERO)
                .currency(CurrencyEnum.CNY)
                .lastTransactionTime(new Date())
                .status(WalletStatusEnum.NORMAL).build());
        return DeveloperResult.success();
    }
}
