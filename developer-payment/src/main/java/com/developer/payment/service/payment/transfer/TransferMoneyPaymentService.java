package com.developer.payment.service.payment.transfer;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.PaymentInfoDTO;
import com.developer.payment.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class TransferMoneyPaymentService implements PaymentService {
    @Override
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto) {
        return null;
    }
}
