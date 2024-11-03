package com.developer.payment.controller;

import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.payment.redpackets.RedPacketsProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("payment")
public class PaymentController {

    @Autowired
    private RedPacketsProxyService redPacketsProxyService;

    @Autowired
    private WalletService walletService;

    /**
     * 发红包
     * @param dto
     * @return
     */
    @PostMapping("red-packets/send")
    public DeveloperResult<Boolean> sendRedPackets(@RequestBody SendRedPacketsDTO dto){
        return redPacketsProxyService.findInstance(dto.getType()).sendRedPackets(dto);
    }

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
