package com.developer.payment.rpc;

import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.dto.TransferInfoDTO;
import com.developer.framework.enums.payment.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.payment.service.processorFactory.PaymentTypeProcessorDispatchFactory;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;
import com.developer.rpc.service.payment.PaymentRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class PaymentRpcProviderService implements PaymentRpcService {

    private final PaymentTypeProcessorDispatchFactory dispatchFactory;

    @Override
    public DeveloperResult<Boolean> invokeRedPacketsTransfer(InvokeRedPacketsTransferRequestRpcDTO dto) {
        PaymentInfoDTO build;
        if(PaymentTypeEnum.RED_PACKETS.equals(dto.getPaymentType())){
            SendRedPacketsDTO redPacketsDTO = new SendRedPacketsDTO();
            build = PaymentInfoDTO.builder().sendRedPacketsDTO(redPacketsDTO).paymentTypeEnum(PaymentTypeEnum.RED_PACKETS).channel(dto.getPaymentChannel()).build();
        }else{
            TransferInfoDTO transferInfoDTO = new TransferInfoDTO(SerialNoHolder.getSerialNo(),dto.getPaymentAmount(),dto.getTargetId(),dto.getPaymentChannel());
            build = PaymentInfoDTO.builder().transferInfoDTO(transferInfoDTO).paymentTypeEnum(PaymentTypeEnum.RED_PACKETS).channel(dto.getPaymentChannel()).build();
        }
        DeveloperResult<SendRedPacketsResultDTO> result = dispatchFactory.getInstance(dto.getPaymentType()).doPay(build);
        if(!result.getIsSuccessful())
        {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),result.getMsg());
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
