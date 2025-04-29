package com.developer.payment.service.impl.redpackets;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import com.developer.payment.enums.WalletOperationTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class LuckRedPacketsServiceImpl extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public RedPacketsTypeEnum redPacketsType() {
        return RedPacketsTypeEnum.LUCKY;
    }

    /**
     * 发红包
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(dto.getSerialNo());

        DeveloperResult<Boolean> result = sendConditionalJudgment(serialNo,dto.getType(),dto.getPaymentChannel(), dto.getTargetId(),userId,dto.getTotalCount(),dto.getRedPacketsAmount());
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(serialNo,result.getMsg());
        }

        // 保存红包信息,lucky明细在领取的时候新增,领取金额实时计算
        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo,userId,dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if(!walletResult.getIsSuccessful()){
            return walletResult;
        }

        // 发送消息事件
        sendRedPacketsMessage(serialNo,dto.getTargetId(),dto.getPaymentChannel());

        // 红包过期退回金额
        long redPacketExpireSeconds = (redPacketsInfoPO.getExpireTime().getTime() - new Date().getTime()) / 1000;
        this.redPacketsRecoveryEvent(serialNo, redPacketsInfoPO.getId(), (int) redPacketExpireSeconds);

        return DeveloperResult.success(serialNo);
    }

    /**
     * 打开红包
     * @param redPacketsId
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> openRedPackets(String serialNo,Long redPacketsId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        RedPacketsInfoPO redPacketsInfo = findRedPacketsCacheInfo(redPacketsId);
        if (redPacketsInfo == null) {
            return DeveloperResult.error(serialNo,"红包不存在");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error(serialNo,"红包已领取完毕");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.EXPIRED)) {
            return DeveloperResult.error(serialNo,"红包已过期无法领取");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.REFUND)) {
            return DeveloperResult.error(serialNo, "红包已退回");
        }

        if(redPacketsInfo.getChannel()==PaymentChannelEnum.FRIEND){
            DeveloperResult<BigDecimal> openResult = this.openPrivateChatRedPackets(serialNo,redPacketsInfo);
            if(!openResult.getIsSuccessful()){
                return openResult;
            }

            // todo 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo,userId,openResult.getData(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if(!walletResult.getIsSuccessful()){
                return DeveloperResult.error(walletResult.getMsg());
            }

            // 红包过期退回金额
            this.redPacketsRecoveryEvent(serialNo,redPacketsId,60*60*24);

            // todo 发送红包领取提示事件给发红包发送人
            redPacketsReceiveNotifyMessage(serialNo,redPacketsInfo.getSenderUserId(),redPacketsInfo.getChannel());

            return openResult;
        }

        BigDecimal openAmount;
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
                    // 更新红包缓存信息
                    updateRedPacketsCacheInfo(redPacketsInfo);
                    redPacketsInfoRepository.updateById(redPacketsInfo);
                }

                redPacketsReceiveDetailsRepository.save(RedPacketsReceiveDetailsPO.builder()
                        .redPacketsId(redPacketsId)
                        .receiveUserId(userId)
                        .receiveAmount(amount)
                        .receiveTime(new Date())
                        .status(RedPacketsReceiveStatusEnum.SUCCESS)
                        .createTime(new Date())
                        .updateTime(new Date())
                        .build());
                updateRedPacketsCacheInfo(redPacketsInfo);
                openAmount = amount;
            }else{
                return DeveloperResult.error(serialNo,"领取失败请重试！");
            }
        }catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeveloperResult.error(serialNo,"领取失败请重试！");
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

        this.redPacketsRecoveryEvent(serialNo,redPacketsId,60*60*24);

        // todo 发送红包领取提示事件给发红包发送人
        redPacketsReceiveNotifyMessage(serialNo,redPacketsInfo.getSenderUserId(),redPacketsInfo.getChannel());

        return DeveloperResult.success(serialNo,openAmount);
    }

    /**
     * 计算红包分配金额
     * @param totalAmount
     * @param totalCount
     * @return
     */
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
