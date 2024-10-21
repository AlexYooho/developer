package com.developer.payment;

import com.developer.payment.dto.PaymentInfoDTO;
import com.developer.payment.service.PaymentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentApplication.class)
public class PaymentTest {

    @Qualifier("redPacketsPaymentService")
    @Autowired
    private PaymentService paymentService;

    @Test
    public void Test(){
        PaymentInfoDTO dto = new PaymentInfoDTO();
        paymentService.doPay(dto);
    }

}
