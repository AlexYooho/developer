package com.developer.payment.service.payment;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.payment.client.FriendClient;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NormalRedPacketsService extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private WalletService walletService;

    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        if (dto.getRedPacketsAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return DeveloperResult.error("红包金额必须大于0");
        }

        if (dto.getTotalCount() <= 0) {
            return DeveloperResult.error("红包数量必须大于0");
        }

        if (dto.getTargetId() <= 0) {
            return DeveloperResult.error("请指定红包接收人！");
        }

        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        DeveloperResult<Boolean> result = receiveTargetProcessor(dto.getChannel(), dto.getTargetId());
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(result.getMsg());
        }

        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);

        // 分配金额
        List<BigDecimal> distributeAmountList = this.distributeRedPacketsAmount(dto.getRedPacketsAmount(), dto.getTotalCount());
        List<RedPacketsReceiveDetailsPO> list = new ArrayList<>();
        for (BigDecimal amount : distributeAmountList) {
            list.add(RedPacketsReceiveDetailsPO.builder()
                    .redPacketsId(redPacketsInfoPO.getId())
                    .receiveUserId(0L)
                    .receiveAmount(amount)
                    .receiveTime(null)
                    .status(RedPacketsReceiveStatusEnum.PENDING)
                    .createTime(new Date())
                    .updateTime(new Date()).build());
        }

        redPacketsInfoRepository.save(redPacketsInfoPO);
        redPacketsReceiveDetailsRepository.saveBatch(list);

        // 处理钱包信息
        walletService.doMoneyTransaction(null, dto.getTargetId(), dto.getRedPacketsAmount());

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets() {
        return null;
    }

}
