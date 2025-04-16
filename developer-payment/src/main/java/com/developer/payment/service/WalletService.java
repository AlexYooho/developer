package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.FreezePayAmountRequestDTO;
import com.developer.payment.dto.WalletRechargeRequestDTO;
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
    DeveloperResult<Boolean> doMoneyTransaction(String serialNo,Long userId,BigDecimal amount, TransactionTypeEnum transactionType, WalletOperationTypeEnum operationType);

    /**
     * 冻结支付金额
     * @param req
     * @return
     */
    DeveloperResult<Boolean> freezePaymentAmount(FreezePayAmountRequestDTO req);

    /**
     * 创建钱包
     * @return
     */
    DeveloperResult<Boolean> createWallet(String serialNo);

    /**
     * 钱包金额充值
     * @param req
     * @return
     */
    DeveloperResult<Boolean> recharge(WalletRechargeRequestDTO req);

}
