package com.developer.payment.service;

import com.developer.framework.model.DeveloperResult;

import java.math.BigDecimal;

public interface WalletService {

    DeveloperResult<Boolean> doMoneyTransfer(Long senderId, Long targetId, BigDecimal amount);

}
