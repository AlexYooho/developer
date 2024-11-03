package com.developer.payment.controller;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.dto.TransferInfoDTO;
import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.TransferDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.payment.redpackets.RedPacketsProxyService;
import com.developer.payment.service.register.PaymentTypeRegister;
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

    @Autowired
    private PaymentTypeRegister paymentTypeRegister;

    /**
     * 发红包
     * @param dto
     * @return
     */
    @PostMapping("red-packets/send")
    public DeveloperResult<Boolean> sendRedPackets(@RequestBody SendRedPacketsDTO dto){
        return paymentTypeRegister.findPaymentTypeInstance(PaymentTypeEnum.RED_PACKETS).doPay(PaymentInfoDTO.builder().sendRedPacketsDTO(dto).paymentTypeEnum(PaymentTypeEnum.RED_PACKETS).channel(dto.getPaymentChannel()).build());
    }

    /**
     * 打开红包
     * @return
     */
    @PostMapping("red-packets/{id}/open")
    public DeveloperResult<BigDecimal> openRedPackets(@PathVariable("id") Long id){
        RedPacketsService instance = redPacketsProxyService.findInstance(id);
        if(instance == null){
            return DeveloperResult.error("红包不存在");
        }
        return instance.openRedPackets(id);
    }

    /**
     * 转账
     * @return
     */
    @PostMapping("transfer")
    public DeveloperResult<Boolean> transfer(@RequestBody TransferInfoDTO dto){
        return paymentTypeRegister.findPaymentTypeInstance(PaymentTypeEnum.TRANSFER).doPay(PaymentInfoDTO.builder().transferInfoDTO(dto).paymentTypeEnum(PaymentTypeEnum.RED_PACKETS).channel(dto.getPaymentChannel()).build());
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
