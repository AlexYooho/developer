package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;

import java.math.BigDecimal;

public interface WalletService {

    /**
     * 发起支付交易
     * @param amount
     * @param transactionType
     * @param operationType
     * @return
     */
    DeveloperResult<Boolean> doMoneyTransaction(BigDecimal amount, TransactionTypeEnum transactionType, WalletOperationTypeEnum operationType);

    /**
     * 冻结支付金额
     * @param amount
     * @return
     */
    DeveloperResult<Boolean> freezePaymentAmount(BigDecimal amount);

    /**
     * 创建钱包
     * @return
     */
    DeveloperResult<Boolean> CreateWallet();

}
