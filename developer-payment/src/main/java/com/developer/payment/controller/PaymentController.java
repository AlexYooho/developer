package com.developer.payment.controller;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.service.payment.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("payment")
public class PaymentController {

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    /**
     * 发红包
     * @return
     */
    @PostMapping("send-red-packets")
    public DeveloperResult<Boolean> sendRedPackets(@RequestBody SendRedPacketsDTO dto){
        return redPacketsTypeRegister.findRedPacketsTypeInstance(dto.getType()).sendRedPackets(dto);
    }

    /**
     * 打开红包
     * @return
     */
    @PostMapping("open-red-packets")
    public DeveloperResult<BigDecimal> openRedPackets(){
        return redPacketsTypeRegister.findRedPacketsTypeInstance(RedPacketsTypeEnum.NORMAL).openRedPackets();
    }

    /**
     * 转账
     * @return
     */
    @PostMapping("transfer-money")
    public DeveloperResult<Boolean> transferMoney(){

        return DeveloperResult.success();
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

}
