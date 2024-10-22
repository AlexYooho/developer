package com.developer.payment;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.payment.dto.PaymentInfoDTO;
import com.developer.payment.dto.TransferInfoDTO;
import com.developer.payment.enums.PaymentTypeEnum;
import com.developer.payment.service.PaymentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentApplication.class)
public class PaymentTest {

    @Qualifier("transferMoneyPaymentService")
    @Autowired
    private PaymentService paymentService;

    @Test
    public void Test(){
        PaymentInfoDTO dto = new PaymentInfoDTO();
        dto.setChannel(PaymentChannelEnum.FRIEND);
        dto.setPaymentTypeEnum(PaymentTypeEnum.TRANSFER);
        dto.setTransferInfoDTO(TransferInfoDTO.builder().transferAmount(BigDecimal.valueOf(1)).targetId(2L).build());
        paymentService.doPay(dto);
    }

}
