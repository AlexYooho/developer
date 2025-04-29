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
        DeveloperResult<Boolean> result = sendConditionalJudgment(serialNo,dto.getType(), dto.getPaymentChannel(), dto.getTargetId(), userId, dto.getTotalCount(),dto.getRedPacketsAmount());
        if (!result.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, result.getMsg());
        }

        // 2、红包信息入库
        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 3、分配金额
        List<BigDecimal> distributeAmountList = this.distributeRedPacketsAmount(dto.getRedPacketsAmount(), dto.getTotalCount());
        if(distributeAmountList.isEmpty()){
            return DeveloperResult.error(serialNo,"分配红包金额计算失败");
        }

        List<RedPacketsReceiveDetailsPO> list = new ArrayList<>();
        for (BigDecimal amount : distributeAmountList) {
            list.add(RedPacketsReceiveDetailsPO.builder()
                    .redPacketsId(redPacketsInfoPO.getId())
                    .receiveUserId(dto.getPaymentChannel() == PaymentChannelEnum.FRIEND ? dto.getTargetId() : 0)
                    .receiveAmount(amount)
                    .receiveTime(null)
                    .status(RedPacketsReceiveStatusEnum.PENDING)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build());
        }
        redPacketsReceiveDetailsRepository.saveBatch(list);

        // 4、处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, userId, dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if (!walletResult.getIsSuccessful()) {
            return walletResult;
        }

        // 5、推送红包消息
        DeveloperResult sendMessageResult = sendRedPacketsMessage(serialNo, dto.getTargetId(), dto.getPaymentChannel(),redPacketsInfoPO.getId());
        if(!sendMessageResult.getIsSuccessful()){
            return DeveloperResult.error(sendMessageResult.getSerialNo(),sendMessageResult.getMsg());
        }

        // 6、推送红包过期延迟检查事件
        long redPacketExpireSeconds = (redPacketsInfoPO.getExpireTime().getTime() - new Date().getTime()) / 1000;
        this.redPacketsRecoveryEvent(serialNo, redPacketsInfoPO.getId(), (int) redPacketExpireSeconds);

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

        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND) {
            if (Objects.equals(redPacketsInfo.getSenderUserId(), SelfUserInfoContext.selfUserInfo().getUserId())) {
                return DeveloperResult.error(serialNo, "无法领取自己发送的私聊红包");
            }
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.FINISHED)) {
            return DeveloperResult.error(serialNo, "红包已领取完毕");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.REFUND)) {
            return DeveloperResult.error(serialNo, "红包已退回");
        }

        if (redPacketsInfo.getStatus().equals(RedPacketsStatusEnum.EXPIRED)) {
            return DeveloperResult.error(serialNo, "红包已过期无法领取");
        }

        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND) {
            DeveloperResult<BigDecimal> openResult = this.openPrivateChatRedPackets(serialNo, redPacketsInfo);
            if (!openResult.getIsSuccessful()) {
                return openResult;
            }

            // todo 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), openResult.getData(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if (!walletResult.getIsSuccessful()) {
                return DeveloperResult.error(serialNo, walletResult.getMsg());
            }

            // todo 发送红包领取提示事件给发红包发送人
            redPacketsReceiveNotifyMessage(serialNo, redPacketsInfo.getSenderUserId(), redPacketsInfo.getChannel());

            // 更新红包缓存信息
            updateRedPacketsCacheInfo(redPacketsInfo);

            return DeveloperResult.success(serialNo, openResult.getData());
        }

        BigDecimal openAmount;
        // 生成分布式锁的key,基于红包Id
        String lockKey = RedisKeyConstant.OPEN_RED_PACKETS_LOCK_KEY(redPacketsId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(30, 5, TimeUnit.SECONDS)) {
                List<RedPacketsReceiveDetailsPO> receiveDetailsList = redPacketsReceiveDetailsRepository.findList(redPacketsId);
                if (!receiveDetailsList.isEmpty()) {
                    RedPacketsReceiveDetailsPO receiveDetails = receiveDetailsList.get(0);
                    receiveDetails.setReceiveUserId(SelfUserInfoContext.selfUserInfo().getUserId());
                    receiveDetails.setReceiveTime(new Date());
                    receiveDetails.setStatus(RedPacketsReceiveStatusEnum.SUCCESS);
                    redPacketsReceiveDetailsRepository.updateById(receiveDetails);
                    openAmount = receiveDetails.getReceiveAmount();
                } else {
                    return DeveloperResult.error(serialNo, "红包抢光啦！");
                }
            } else {
                return DeveloperResult.error(serialNo, "领取失败请重试！");
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

        // todo 发送红包领取提示事件给发红包发送人
        redPacketsReceiveNotifyMessage(serialNo, redPacketsInfo.getSenderUserId(), redPacketsInfo.getChannel());

        // 更新红包缓存信息
        updateRedPacketsCacheInfo(redPacketsInfo);

        return DeveloperResult.success(serialNo, openAmount);
    }
}
