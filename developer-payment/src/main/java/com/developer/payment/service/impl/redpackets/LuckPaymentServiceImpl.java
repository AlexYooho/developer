package com.developer.payment.service.impl.redpackets;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.enums.payment.PaymentTypeEnum;
import com.developer.framework.enums.payment.RedPacketsTypeEnum;
import com.developer.framework.exception.DeveloperBusinessException;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.framework.enums.payment.RedPacketsReceiveStatusEnum;
import com.developer.framework.enums.payment.RedPacketsStatusEnum;
import com.developer.framework.enums.payment.TransactionTypeEnum;
import com.developer.framework.enums.payment.WalletOperationTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.WalletService;
import com.developer.payment.service.impl.BasePaymentService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class LuckPaymentServiceImpl extends BasePaymentService implements RedPacketsService {

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
     *
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<SendRedPacketsResultDTO> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();

        // 1、红包发送条件判断
        DeveloperResult<Boolean> result = paymentCommentConditionalJudgment(serialNo, PaymentTypeEnum.RED_PACKETS,dto.getPaymentChannel(),dto.getType(),dto.getRedPacketsAmount(),dto.getTargetId(),userId,dto.getTotalCount());
        if (!result.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, result.getMsg());
        }

        // 2、红包信息入库,lucky明细在领取的时候新增,领取金额实时计算
        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 3、处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, userId, dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if (!walletResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,walletResult.getMsg());
        }

        // 4、推送红包消息
        DeveloperResult sendMessageResult = sendRedPacketsMessage(serialNo, dto.getTargetId(), dto.getPaymentChannel(), redPacketsInfoPO.getId(),PaymentTypeEnum.RED_PACKETS,"红包来啦", MessageContentTypeEnum.RED_PACKETS);
        if (!sendMessageResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,sendMessageResult.getMsg());
        }

        // 5、红包过期退回金额
        this.transactionExpiredCheckEvent(serialNo,PaymentTypeEnum.RED_PACKETS, redPacketsInfoPO.getId(), redPacketsInfoPO.getExpireTime().getTime());

        return DeveloperResult.success(serialNo);
    }

    /**
     * 打开红包
     *
     * @param redPacketsId
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> openRedPackets(String serialNo, Long redPacketsId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        RedPacketsInfoPO redPacketsInfo = findRedPacketsCacheInfo(redPacketsId);
        if (redPacketsInfo == null) {
            return DeveloperResult.error(serialNo, "红包不存在");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error(serialNo, "红包已经空了");
        }

        BigDecimal openAmount;
        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND) {

            if (Objects.equals(redPacketsInfo.getSenderUserId(), userId)) {
                return DeveloperResult.error(serialNo, "无法领取自己发送的私聊红包");
            }

            // 判断是否领取人为当前用户
            if (!userId.equals(redPacketsInfo.getReceiveTargetId())) {
                return DeveloperResult.error(serialNo, "不是给你的红包哦");
            }

            openAmount = this.openPrivateChatRedPackets(redPacketsInfo);
            if(Objects.equals(openAmount, BigDecimal.ZERO)){{
                return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
            }}

            // 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, userId, openAmount, TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if (!walletResult.getIsSuccessful()) {
                throw new DeveloperBusinessException(serialNo,walletResult.getMsg());
            }

        } else {
            // 群聊红包
            // 判断当前用户是否已经领取过该红包了
            RedPacketsReceiveDetailsPO redPacketsDetails = redPacketsReceiveDetailsRepository.find(redPacketsId,SelfUserInfoContext.selfUserInfo().getUserId());
            if(redPacketsDetails!=null){
                return DeveloperResult.error(serialNo, "无法重复领取同一个红包");
            }

            // 生成分布式锁的key,基于红包Id
            String lockKey = RedisKeyConstant.OPEN_RED_PACKETS_LOCK_KEY(redPacketsId);
            RLock lock = redissonClient.getLock(lockKey);
            try {
                if (lock.tryLock(30, 5, TimeUnit.SECONDS)) {
                    openAmount = distributeRedPacketsAmount(redPacketsInfo.getSendAmount(), redPacketsInfo.getRemainingCount());
                    if (Objects.equals(openAmount, BigDecimal.ZERO)) {
                        return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
                    }

                    // 更新红包信息
                    redPacketsInfo.setRemainingAmount(redPacketsInfo.getRemainingAmount().subtract(openAmount));
                    redPacketsInfo.setRemainingCount(redPacketsInfo.getRemainingCount() - 1);
                    redPacketsInfo.setStatus(redPacketsInfo.getRemainingCount() == 0 ? RedPacketsStatusEnum.FINISHED : redPacketsInfo.getStatus());
                    redPacketsInfoRepository.updateById(redPacketsInfo);
                    redPacketsReceiveDetailsRepository.save(RedPacketsReceiveDetailsPO.builder()
                            .redPacketsId(redPacketsId)
                            .receiveUserId(userId)
                            .receiveAmount(openAmount)
                            .receiveTime(new Date())
                            .status(RedPacketsReceiveStatusEnum.SUCCESS)
                            .createTime(new Date())
                            .updateTime(new Date())
                            .build());
                } else {
                    return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new DeveloperBusinessException(serialNo,"领取失败请重试！");
            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        //更新红包缓存信息
        updateRedPacketsCacheInfo(redPacketsInfo);

        // 发送红包领取提示事件给发红包发送人
        redPacketsReceiveNotifyMessage(serialNo, redPacketsInfo.getSenderUserId(), redPacketsInfo.getChannel());

        return DeveloperResult.success(serialNo, openAmount);
    }

    /**
     * 计算红包分配金额
     *
     * @param totalAmount
     * @param totalCount
     * @return
     */
    @Override
    public BigDecimal distributeRedPacketsAmount(BigDecimal totalAmount, Integer totalCount) {
        if (totalCount <= 0) {
            throw new DeveloperBusinessException("","红包个数必须大于零！");
        }

        if (totalAmount.compareTo(BigDecimal.valueOf(0.01).multiply(BigDecimal.valueOf(totalCount))) < 0) {
            throw new DeveloperBusinessException("","总金额不足以分配每个红包最少 0.01 元！");
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

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
