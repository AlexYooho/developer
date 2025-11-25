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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class NormalPaymentServiceImpl extends BasePaymentService implements RedPacketsService {

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
    public DeveloperResult<SendRedPacketsResultDTO> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();

        // 1、红包发送条件判断
        DeveloperResult<Boolean> result = paymentCommentConditionalJudgment(serialNo, PaymentTypeEnum.RED_PACKETS,dto.getPaymentChannel(),dto.getType(),dto.getRedPacketsAmount(),dto.getTargetId(),userId,dto.getTotalCount());
        if (!result.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,result.getMsg());
        }

        // 2、红包信息入库
        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 3、处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, userId, dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if (!walletResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,walletResult.getMsg());
        }

        // 4、推送红包消息
        DeveloperResult<SendRedPacketsResultDTO> sendMessageResult = sendRedPacketsMessage(serialNo, dto.getTargetId(), dto.getPaymentChannel(), redPacketsInfoPO.getId(),PaymentTypeEnum.RED_PACKETS,"红包来啦", MessageContentTypeEnum.RED_PACKETS);
        if (!sendMessageResult.getIsSuccessful()) {
            throw new DeveloperBusinessException(serialNo,sendMessageResult.getMsg());
        }

        // 5、推送红包过期延迟检查事件
        this.transactionExpiredCheckEvent(serialNo,PaymentTypeEnum.RED_PACKETS, redPacketsInfoPO.getId(), redPacketsInfoPO.getExpireTime().getTime());

        SendRedPacketsResultDTO resultDto = new SendRedPacketsResultDTO();
        resultDto.setMessageId(sendMessageResult.getData().getMessageId());

        return DeveloperResult.success(serialNo,resultDto);
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

        BigDecimal openAmount;
        // 1、私聊红包
        if (redPacketsInfo.getChannel() == PaymentChannelEnum.FRIEND) {

            if (Objects.equals(redPacketsInfo.getSenderUserId(), SelfUserInfoContext.selfUserInfo().getUserId())) {
                return DeveloperResult.error(serialNo, "无法领取自己发送的私聊红包");
            }

            // 判断是否领取人为当前用户
            if(!SelfUserInfoContext.selfUserInfo().getUserId().equals(redPacketsInfo.getReceiveTargetId())){
                return DeveloperResult.error(serialNo,"不是给你的红包哦");
            }

            openAmount = this.openPrivateChatRedPackets(redPacketsInfo);
            if(Objects.equals(openAmount, BigDecimal.ZERO)){{
                return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
            }}

            // 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(serialNo, SelfUserInfoContext.selfUserInfo().getUserId(), openAmount, TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if (!walletResult.getIsSuccessful()) {
                throw new DeveloperBusinessException(serialNo,walletResult.getMsg());
            }

        } else {
            // 2、群组红包

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
                    // 计算领取金额
                    openAmount = this.distributeRedPacketsAmount(redPacketsInfo.getRemainingAmount(), redPacketsInfo.getRemainingCount());
                    if (Objects.equals(openAmount, BigDecimal.ZERO)) {
                        return DeveloperResult.error(serialNo, "手速慢啦,红包抢光啦！");
                    }

                    // 更新红包信息
                    redPacketsInfo.setRemainingAmount(redPacketsInfo.getRemainingAmount().subtract(openAmount));
                    redPacketsInfo.setRemainingCount(redPacketsInfo.getRemainingCount() - 1);
                    redPacketsInfo.setStatus(redPacketsInfo.getRemainingCount() == 0 ? RedPacketsStatusEnum.FINISHED : redPacketsInfo.getStatus());
                    redPacketsInfoRepository.updateById(redPacketsInfo);

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
                throw new DeveloperBusinessException(serialNo,"领取失败请重试！");
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
