package com.developer.payment.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.FreezePayAmountRequestDTO;
import com.developer.payment.dto.WalletRechargeRequestDTO;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return walletService.createWallet();
    }

    /**
     * 冻结支付金额
     * @param req
     * @return
     */
    @PostMapping("freeze-pay-amount")
    public DeveloperResult<Boolean> freezePayAmount(@RequestBody FreezePayAmountRequestDTO req){
        return walletService.freezePaymentAmount(req);
    }

    /**
     * 充值
     * @param req
     * @return
     */
    @PostMapping("recharge")
    public DeveloperResult<Boolean> recharge(@RequestBody WalletRechargeRequestDTO req){
        return walletService.recharge(req);
    }
}
