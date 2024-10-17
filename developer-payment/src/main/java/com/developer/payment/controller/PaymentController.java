package com.developer.payment.controller;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.payment.redpackets.RedPacketsProxyService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("payment")
public class PaymentController {

    @Autowired
    private RedPacketsProxyService redPacketsProxyService;

    @Autowired
    private WalletService walletService;

    /**
     * 打开红包
     * @return
     */
    @PostMapping("red-packets/{id}/open")
    public DeveloperResult<BigDecimal> openRedPackets(@PathVariable("id") Long id){
        return redPacketsProxyService.findInstance(id).openRedPackets(id);
    }

    /**
     * 确认收款转账金额
     * @return
     */
    @PostMapping("confirm-receipt-transfer")
    public DeveloperResult<Boolean> confirmReceiptTransfer(){
        return DeveloperResult.success();
    }

    /**
     * 退回转账
     * @return
     */
    @PostMapping("return-transfer")
    public DeveloperResult<Boolean> returnTransfer(){
        return DeveloperResult.success();
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
