package com.developer.payment.service.payment.redpackets;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.payment.client.FriendClient;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NormalRedPacketsService extends BaseRedPacketsService implements RedPacketsService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto, PaymentChannelEnum paymentChannel) {
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
        DeveloperResult<Boolean> result = receiveTargetProcessor(paymentChannel, dto.getTargetId());
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(result.getMsg());
        }

        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto,paymentChannel);

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
                List<RedPacketsReceiveDetailsPO> receiveDetailsList = redPacketsReceiveDetailsRepository.findList(redPacketsId);
                if(!receiveDetailsList.isEmpty()){
                    RedPacketsReceiveDetailsPO receiveDetails = receiveDetailsList.get(0);
                    receiveDetails.setReceiveUserId(userId);
                    receiveDetails.setReceiveTime(new Date());
                    receiveDetails.setStatus(RedPacketsReceiveStatusEnum.SUCCESS);
                    redPacketsReceiveDetailsRepository.updateById(receiveDetails);

                    openAmount = receiveDetails.getReceiveAmount();
                }
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

}
