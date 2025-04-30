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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class NormalRedPacketsServiceImpl extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;


    @Override
    public RedPacketsTypeEnum redPacketsType() {
        return RedPacketsTypeEnum.NORMAL;
    }

    /**
     * 发红包
     *
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(dto.getSerialNo());

        // 1、红包发送条件判断
        DeveloperResult<Boolean> result = sendConditionalJudgment(serialNo, dto.getType(), dto.getPaymentChannel(), dto.getTargetId(), userId, dto.getTotalCount(), dto.getRedPacketsAmount());
        if (!result.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, result.getMsg());
        }

        // 2、处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, userId, dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if (!walletResult.getIsSuccessful()) {
            return walletResult;
        }

        // 3、红包信息入库
        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 4、推送红包消息
        DeveloperResult sendMessageResult = sendRedPacketsMessage(serialNo, dto.getTargetId(), dto.getPaymentChannel(), redPacketsInfoPO.getId());
        if (!sendMessageResult.getIsSuccessful()) {
            return DeveloperResult.error(sendMessageResult.getSerialNo(), sendMessageResult.getMsg());
        }

        // 5、推送红包过期延迟检查事件
        this.redPacketsRecoveryEvent(serialNo, redPacketsInfoPO.getId(), redPacketsInfoPO.getExpireTime().getTime());

        return DeveloperResult.success(serialNo);
    }

    /**
     * 抢红包--要区分私发和群发
     *
     * @param redPacketsId
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> openRedPackets(String serialNo, Long redPacketsId) {
        RedPacketsInfoPO redPacketsInfo = findRedPacketsCacheInfo(redPacketsId);
        if (redPacketsInfo == null) {
            return DeveloperResult.error(serialNo, "红包不存在");
        }

        if(redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)){
            return DeveloperResult.error(serialNo, "红包已经空了");
        }

        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND && Objects.equals(redPacketsInfo.getSenderUserId(), SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(serialNo, "无法领取自己发送的私聊红包");
        }

        BigDecimal openAmount;
        // 1、私聊红包
        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND) {

            // 判断是否领取人为当前用户
            if(!SelfUserInfoContext.selfUserInfo().getUserId().equals(redPacketsInfo.getReceiveTargetId())){
                return DeveloperResult.error(serialNo,"不是给你的红包哦");
            }

            openAmount = this.openPrivateChatRedPackets(redPacketsInfo);

            // 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), openAmount, TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if (!walletResult.getIsSuccessful()) {
                return DeveloperResult.error(serialNo, walletResult.getMsg());
            }

        } else {
            // 2、群组红包

            // 判断当前用户是否已经领取过该红包了
            RedPacketsReceiveDetailsPO redPacketsDetails = redPacketsReceiveDetailsRepository.find(redPacketsId);
            if(redPacketsDetails!=null){
                return DeveloperResult.error(serialNo, "无法重复领取同一个红包");
            }

            // 生成分布式锁的key,基于红包Id
            String lockKey = RedisKeyConstant.OPEN_RED_PACKETS_LOCK_KEY(redPacketsId);
            RLock lock = redissonClient.getLock(lockKey);
            try {
                if (lock.tryLock(30, 5, TimeUnit.SECONDS)) {
                    // 计算领取金额
                    openAmount = this.distributeRedPacketsAmount(redPacketsInfo.getRemainingAmount(), redPacketsInfo.getRemainingCount());
                    if (Objects.equals(openAmount, BigDecimal.ZERO)) {
                        return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
                    }

                    // 记录领取明细
                    RedPacketsReceiveDetailsPO detailsPO = RedPacketsReceiveDetailsPO.builder()
                            .redPacketsId(redPacketsInfo.getId())
                            .receiveUserId(SelfUserInfoContext.selfUserInfo().getUserId())
                            .receiveAmount(openAmount)
                            .receiveTime(new Date())
                            .status(RedPacketsReceiveStatusEnum.SUCCESS)
                            .createTime(new Date())
                            .updateTime(new Date())
                            .build();
                    redPacketsReceiveDetailsRepository.save(detailsPO);
                } else {
                    return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return DeveloperResult.error(serialNo, "领取失败请重试！");
            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        // 发送红包领取提示事件给发红包发送人
        redPacketsReceiveNotifyMessage(serialNo, redPacketsInfo.getSenderUserId(), redPacketsInfo.getChannel());

        // 更新红包缓存信息
        updateRedPacketsCacheInfo(redPacketsInfo);

        return DeveloperResult.success(serialNo, openAmount);
    }
}
