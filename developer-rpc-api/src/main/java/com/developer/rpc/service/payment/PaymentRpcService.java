package com.developer.rpc.service.payment;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;

public interface PaymentRpcService {

    DeveloperResult<Boolean> invokeRedPacketsTransfer(InvokeRedPacketsTransferRequestRpcDTO dto);

}
