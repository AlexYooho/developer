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
    public DeveloperResult<Boolean> doMoneyTransfer(Long senderId, Long targetId, BigDecimal amount) {

        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if(walletInfo==null){
            return DeveloperResult.error("用户不存在");
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
                .relatedUserId(targetId).referenceId("").status(TransactionStatusEnum.PENDING).createdTime(new Date()).updateTime(new Date()).build());

        return DeveloperResult.success();
    }
}