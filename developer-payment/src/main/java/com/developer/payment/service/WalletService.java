package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.enums.TransactionTypeEnum;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

public interface WalletService {

    /**
     * 发起支付交易
     * @param context
     * @param payeeId
     * @param amount
     * @return
     */
    @TwoPhaseBusinessAction(name = "doMoneyTransaction", commitMethod = "confirmTransaction", rollbackMethod = "cancelTransaction")
    DeveloperResult<Boolean> doMoneyTransaction(BusinessActionContext context, Long payeeId, BigDecimal amount, TransactionTypeEnum transactionType);

    /**
     * 确认支付交易
     * @param context
     * @return
     */
    DeveloperResult<Boolean> confirmTransaction(BusinessActionContext context);

    /**
     * 撤销支付交易
     * @param context
     * @return
     */
    DeveloperResult<Boolean> cancelTransaction(BusinessActionContext context);

    /**
     * 冻结支付金额
     * @param amount
     * @return
     */
    DeveloperResult<Boolean> freezePaymentAmount(BigDecimal amount);

}
