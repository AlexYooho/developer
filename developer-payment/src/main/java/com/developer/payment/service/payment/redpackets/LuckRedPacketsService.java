package com.developer.payment.service.payment.redpackets;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SendRedPacketsDTO;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.payment.BaseRedPacketsService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class LuckRedPacketsService extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

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
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        RedPacketsInfoPO redPacketsInfo = findRedPacketsCacheInfo(redPacketsId);
        if (redPacketsInfo == null) {
            return DeveloperResult.error("红包不存在");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error("红包已领取完毕");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.EXPIRED)) {
            return DeveloperResult.error("红包已过期无法领取");
        }

        BigDecimal openAmount = BigDecimal.ZERO;

        // 生成分布式锁的key,基于红包Id
        String lockKey = RedisKeyConstant.OPEN_RED_PACKETS_LOCK_KEY(redPacketsId);
        RLock lock = redissonClient.getLock(lockKey);
        try{
            if(lock.tryLock(30,5, TimeUnit.SECONDS)){
                BigDecimal amount = distributeRedPacketsAmount(redPacketsInfo.getSendAmount(), redPacketsInfo.getRemainingCount()).get(0);
                redPacketsInfo.setRemainingAmount(redPacketsInfo.getRemainingAmount().subtract(amount));
                redPacketsInfo.setRemainingCount(redPacketsInfo.getRemainingCount() - 1);
                if(redPacketsInfo.getRemainingCount()==0){
                    redPacketsInfo.setStatus(RedPacketsStatusEnum.FINISHED);
                    redPacketsInfoRepository.updateById(redPacketsInfo);
                }

                RedPacketsReceiveDetailsPO receiveDetailsPO = RedPacketsReceiveDetailsPO.builder().redPacketsId(redPacketsId).receiveUserId(userId).receiveAmount(amount).receiveTime(new Date())
                        .status(RedPacketsReceiveStatusEnum.SUCCESS).build();
                redPacketsReceiveDetailsRepository.save(receiveDetailsPO);
                updateRedPacketsCacheInfo(redPacketsInfo);
            }else{
                return DeveloperResult.error("领取失败请重试！");
            }
        }catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeveloperResult.error("领取失败请重试！");
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return DeveloperResult.success(openAmount);
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
