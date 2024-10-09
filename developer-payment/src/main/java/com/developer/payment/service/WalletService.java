package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

public interface WalletService {

    @TwoPhaseBusinessAction(name = "doMoneyTransaction", commitMethod = "confirmTransaction", rollbackMethod = "cancelTransaction")
    DeveloperResult<Boolean> doMoneyTransaction(BusinessActionContext context,Long senderId, Long targetId, BigDecimal amount);

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

}
