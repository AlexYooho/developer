package com.developer.payment.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.CurrencyEnum;
import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.client.MessageClient;
import com.developer.payment.dto.CheckVerifyCodeRequestDTO;
import com.developer.payment.dto.FreezePayAmountRequestDTO;
import com.developer.payment.dto.WalletRechargeRequestDTO;
import com.developer.payment.enums.TransactionStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.enums.WalletStatusEnum;
import com.developer.payment.pojo.UserWalletPO;
import com.developer.payment.pojo.WalletTransactionRecordPO;
import com.developer.payment.repository.UserWalletRepository;
import com.developer.payment.repository.WalletTransactionRecordRepository;
import com.developer.payment.service.WalletService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    /**
     * 发起交易
     * @param amount
     * @return
     */
    @Override
    @GlobalTransactional(name = "wallet-transaction-tx", rollbackFor = Exception.class)
    public DeveloperResult<Boolean> doMoneyTransaction(String serialNo, Long userId, BigDecimal amount, TransactionTypeEnum transactionType, WalletOperationTypeEnum operationType) {
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if (walletInfo == null) {
            return DeveloperResult.error(serialNo, "用户未开通钱包");
        }

        if (operationType == WalletOperationTypeEnum.EXPENDITURE && walletInfo.getBalance().compareTo(amount) < 0) {
            return DeveloperResult.error(serialNo, "余额不足");
        }

        if (walletInfo.getStatus() == WalletStatusEnum.FROZEN) {
            return DeveloperResult.error(serialNo, "钱包被冻结");
        }

        BigDecimal beforeBalance = walletInfo.getBalance();
        BigDecimal afterBalance = operationType == WalletOperationTypeEnum.EXPENDITURE ? walletInfo.getBalance().subtract(amount.abs()) : walletInfo.getBalance().add(amount.abs());

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

        return DeveloperResult.success(serialNo);
    }

    /**
     * 冻结支付金额
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> freezePaymentAmount(FreezePayAmountRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);

        if (walletInfo.getBalance().compareTo(req.getAmount()) < 0) {
            return DeveloperResult.error(serialNo, "余额不足");
        }

        walletInfo.setFrozenBalance(walletInfo.getFrozenBalance().add(req.getAmount()));
        walletInfo.setUpdateTime(new Date());
        walletRepository.updateById(walletInfo);

        return DeveloperResult.success(serialNo);
    }

    /**
     * 创建钱包
     * @return
     */
    @Override
    public DeveloperResult<Boolean> createWallet(String serialNo) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        serialNo = snowflakeNoUtil.getSerialNo(serialNo);
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if (walletInfo != null) {
            return DeveloperResult.error(serialNo, "用户已开通钱包");
        }

        walletRepository.save(UserWalletPO.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .paymentPassword(-1)
                .frozenBalance(BigDecimal.ZERO)
                .totalRecharge(BigDecimal.ZERO)
                .totalWithdraw(BigDecimal.ZERO)
                .currency(CurrencyEnum.CNY)
                .lastTransactionTime(new Date())
                .status(WalletStatusEnum.NORMAL)
                .createTime(new Date())
                .updateTime(new Date())
                .build());
        return DeveloperResult.success(serialNo);
    }

    /**
     * 钱包金额充值
     * @param req
     * @return
     */
    @Override
    @GlobalTransactional(name = "wallet-recharge-tx", rollbackFor = Exception.class)
    public DeveloperResult<Boolean> recharge(WalletRechargeRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();

        // 验证入参
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return DeveloperResult.error(serialNo, "充值金额无效");
        }
        if (req.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            return DeveloperResult.error(serialNo, "充值金额超过上限");
        }
        if (req.getPaymentPassword() == null) {
            return DeveloperResult.error(serialNo, "支付密码不能为空");
        }

        // 钱包校验
        UserWalletPO walletInfo = walletRepository.findByUserId(userId);
        if (walletInfo == null) {
            return DeveloperResult.error(serialNo, "钱包不存在");
        }

        if (walletInfo.getStatus() != WalletStatusEnum.NORMAL) {
            return DeveloperResult.error(serialNo, "钱包状态异常");
        }

        if (!walletInfo.getPaymentPassword().equals(req.getPaymentPassword())) {
            return DeveloperResult.error(serialNo, "支付密码错误");
        }

        BigDecimal beforeBalance = walletInfo.getBalance();

        // 更新钱包余额和累计充值金额
        walletInfo.setBalance(walletInfo.getBalance().add(req.getAmount()));
        walletInfo.setTotalRecharge(walletInfo.getTotalRecharge().add(req.getAmount()));

        walletRepository.updateById(walletInfo);

        // 交易流水
        walletTransactionRepository.save(WalletTransactionRecordPO.builder()
                .walletId(walletInfo.getId())
                .userId(userId)
                .transactionType(TransactionTypeEnum.RECHARGE)
                .amount(req.getAmount())
                .beforeBalance(beforeBalance)
                .afterBalance(walletInfo.getBalance())
                .relatedUserId(userId)
                .referenceId("")
                .status(TransactionStatusEnum.SUCCESS)
                .createdTime(new Date())
                .updateTime(new Date())
                .build());

        return DeveloperResult.success(serialNo);
    }
}
