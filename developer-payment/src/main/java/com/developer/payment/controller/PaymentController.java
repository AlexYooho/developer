package com.developer.payment.controller;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.dto.TransferInfoDTO;
import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;
import com.developer.payment.service.register.PaymentTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("payment")
public class PaymentController {

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
    @PostMapping("red-packets/open")
    public DeveloperResult<BigDecimal> openRedPackets(@RequestBody OpenRedPacketsRequestDTO req){
        return paymentTypeRegister.findPaymentTypeInstance(PaymentTypeEnum.RED_PACKETS).amountCharged(req);
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
    public DeveloperResult<BigDecimal> confirmReceiptTransfer(@RequestBody OpenRedPacketsRequestDTO dto){
        return paymentTypeRegister.findPaymentTypeInstance(PaymentTypeEnum.TRANSFER).amountCharged(dto);
    }

    /**
     * 退回转账
     * @return
     */
    @PostMapping("return-transfer")
    public DeveloperResult<Boolean> returnTransfer(@RequestBody ReturnTransferRequestDTO req){
        return paymentTypeRegister.findPaymentTypeInstance(PaymentTypeEnum.TRANSFER).amountRefunded(req);
    }



}
