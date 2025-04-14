package com.developer.payment.service.impl.redpackets;

import com.developer.framework.enums.PaymentTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.dto.OpenRedPacketsRequestDTO;
import com.developer.payment.dto.ReturnTransferRequestDTO;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.processorFactory.RedPacketsTypeProcessorDispatchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class RedPacketsPaymentServiceImpl implements PaymentService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Autowired
    private RedPacketsTypeProcessorDispatchFactory dispatchFactory;

    @Override
    public PaymentTypeEnum paymentType() {
        return PaymentTypeEnum.RED_PACKETS;
    }

    /**
     * 支付红包
     * @param dto
     */
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto){
        return dispatchFactory.getInstance(dto.getSendRedPacketsDTO().getType()).sendRedPackets(dto.getSendRedPacketsDTO());
    }

    /**
     * 领取红包
     * @param req
     */
    @Override
    public DeveloperResult<BigDecimal> amountCharged(OpenRedPacketsRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        RedPacketsInfoPO po = redPacketsInfoRepository.getById(req.getRedPacketsId());
        if (po == null) {
            return DeveloperResult.error(serialNo,"红包不存在");
        }
        return dispatchFactory.getInstance(po.getType()).openRedPackets(req.getSerialNo(), req.getRedPacketsId());
    }

    /**
     * 退回金额
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> amountRefunded(ReturnTransferRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        RedPacketsInfoPO redPacketsInfo = redPacketsInfoRepository.getById(req.getRedPacketsId());
        if (redPacketsInfo == null) {
            return DeveloperResult.error(serialNo,"红包不存在");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error(serialNo,"红包已领取,无法退回");
        }

        redPacketsInfo.setStatus(RedPacketsStatusEnum.REFUND);
        redPacketsInfo.setReturnAmount(redPacketsInfo.getRemainingAmount());
        redPacketsInfo.setUpdateTime(new Date());
        redPacketsInfoRepository.updateById(redPacketsInfo);

        return DeveloperResult.success(serialNo);
    }
}
