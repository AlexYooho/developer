package com.developer.payment.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * 开通钱包
     * @return
     */
    @PostMapping("create")
    public DeveloperResult<Boolean> CreateWallet(){
        return walletService.CreateWallet();
    }

    /**
     * 冻结支付金额
     * @param amount
     * @return
     */
    @PostMapping("freeze-pay-amount")
    public DeveloperResult<Boolean> freezePayAmount(BigDecimal amount){
        return walletService.freezePaymentAmount(amount);
    }

}
