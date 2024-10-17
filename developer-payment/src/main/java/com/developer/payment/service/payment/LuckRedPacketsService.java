package com.developer.payment.service.payment;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.RedPacketsChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.payment.client.FriendClient;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
public class LuckRedPacketsService extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

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
            return DeveloperResult.error("请选择红包发送渠道！");
        }

        DeveloperResult<Boolean> result = receiveTargetProcessor(dto.getChannel(), dto.getTargetId());
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(result.getMsg());
        }

        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);

        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 处理钱包信息
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        walletService.doMoneyTransaction(null, dto.getTargetId(), dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET);

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult<BigDecimal> openRedPackets(Long redPacketsId) {
        return null;
    }

    @Override
    public List<BigDecimal> distributeRedPacketsAmount(BigDecimal totalAmount, Integer totalCount) {
        if (totalCount <= 0) {
            throw new IllegalArgumentException("红包个数必须大于零");
        }

        if (totalAmount.compareTo(BigDecimal.valueOf(0.01).multiply(BigDecimal.valueOf(totalCount))) < 0) {
            throw new IllegalArgumentException("总金额不足以分配每个红包最少 0.01 元");
        }

        List<BigDecimal> list = new ArrayList<>();
        BigDecimal remainingAmount = totalAmount; // 剩余金额
        Random random = new Random();

        for (int i = 0; i < totalCount - 1; i++) {
            // 计算随机红包金额的最大值
            BigDecimal max = remainingAmount.divide(BigDecimal.valueOf(totalCount - i).multiply(BigDecimal.valueOf(2)), 2, RoundingMode.DOWN);
            BigDecimal randomAmount = BigDecimal.valueOf(0.01).add(BigDecimal.valueOf(random.nextDouble()).multiply(max.subtract(BigDecimal.valueOf(0.01))));

            randomAmount = randomAmount.setScale(2, RoundingMode.DOWN); // 保留两位小数
            list.add(randomAmount); // 分配给当前红包
            remainingAmount = remainingAmount.subtract(randomAmount); // 更新剩余金额
        }

        // 剩余的金额放到最后一个红包
        list.add(remainingAmount.setScale(2, RoundingMode.DOWN));

        return list;
    }
}
