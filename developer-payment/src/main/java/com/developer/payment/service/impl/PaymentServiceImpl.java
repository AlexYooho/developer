package com.developer.payment.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.enums.TransactionStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletStatusEnum;
import com.developer.payment.pojo.UserWalletPO;
import com.developer.payment.pojo.WalletTransactionPO;
import com.developer.payment.repository.UserWalletRepository;
import com.developer.payment.repository.WalletTransactionRepository;
import com.developer.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private UserWalletRepository userWalletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Transactional
    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        UserWalletPO walletInfo = userWalletRepository.findByUserId(userId);
        if(walletInfo==null){
            return DeveloperResult.error("用户不存在");
        }

        if (walletInfo.getBalance().compareTo(dto.getRedPacketsAmount()) < 0) {
            return DeveloperResult.error("余额不足");
        }

        if(walletInfo.getStatus() == WalletStatusEnum.FROZEN){
            return DeveloperResult.error("钱包被冻结");
        }

        BigDecimal beforeBalance = walletInfo.getBalance();
        BigDecimal afterBalance = walletInfo.getBalance().subtract(dto.getRedPacketsAmount());

        walletInfo.setLastTransactionTime(new Date());
        walletInfo.setUpdateTime(new Date());
        walletInfo.setBalance(walletInfo.getBalance().subtract(dto.getRedPacketsAmount()));
        userWalletRepository.updateById(walletInfo);

        WalletTransactionPO walletTransaction = WalletTransactionPO.builder()
                .walletId(walletInfo.getId())
                .userId(userId)
                .transactionType(TransactionTypeEnum.RED_PACKET)
                .beforeBalance(beforeBalance)
                .amount(dto.getRedPacketsAmount())
                .afterBalance(afterBalance)
                .relatedUserId(dto.getTargetId())
                .status(TransactionStatusEnum.PENDING)
                .createdTime(new Date())
                .updateTime(new Date())
                .build();
        walletTransactionRepository.save(walletTransaction);

        log.info("用户:{} 发送红包:{} 元",userId,dto.getRedPacketsAmount());

        return null;
    }
}
