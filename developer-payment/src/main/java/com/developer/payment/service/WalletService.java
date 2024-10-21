package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.enums.TransactionTypeEnum;

import java.math.BigDecimal;

public interface WalletService {

    /**
     * 发起支付交易
     * @param payeeId
     * @param amount
     * @return
     */
    DeveloperResult<Boolean> doMoneyTransaction(Long payeeId, BigDecimal amount, TransactionTypeEnum transactionType);

    /**
     * 冻结支付金额
     * @param amount
     * @return
     */
    DeveloperResult<Boolean> freezePaymentAmount(BigDecimal amount);

}
