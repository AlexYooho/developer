package com.developer.payment.service.payment.redpackets;

import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class RedPacketsPaymentService implements PaymentService {

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    /**
     * 支付红包
     * @param dto
     */
    public DeveloperResult<Boolean> doPay(PaymentInfoDTO dto){
        return redPacketsTypeRegister.findInstance(dto.getSendRedPacketsDTO().getType()).sendRedPackets(dto.getSendRedPacketsDTO());
    }

    /**
     * 领取红包
     * @param id
     */
    @Override
    public DeveloperResult<BigDecimal> confirmReceipt(Long id) {
        RedPacketsInfoPO po = redPacketsInfoRepository.getById(id);
        if (po == null) {
            return DeveloperResult.error("红包不存在");
        }
        return redPacketsTypeRegister.findInstance(po.getType()).openRedPackets(id);
    }

    /**
     * 退回金额
     * @param id
     * @return
     */
    @Override
    public DeveloperResult<Boolean> amountRefunded(Long id) {
        RedPacketsInfoPO redPacketsInfo = redPacketsInfoRepository.getById(id);
        if (redPacketsInfo == null) {
            return DeveloperResult.error("红包不存在");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error("红包已领取,无法退回");
        }

        redPacketsInfo.setStatus(RedPacketsStatusEnum.REFUND);
        redPacketsInfo.setReturnAmount(redPacketsInfo.getRemainingAmount());
        redPacketsInfo.setUpdateTime(new Date());
        redPacketsInfoRepository.updateById(redPacketsInfo);

        return DeveloperResult.success();
    }
}
