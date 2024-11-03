package com.developer.payment.service.payment.redpackets;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.dto.SendRedPacketsDTO;
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
import com.developer.payment.service.payment.BaseRedPacketsService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    /**
     * 发红包
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> sendRedPackets(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        if (dto.getRedPacketsAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return DeveloperResult.error("红包金额必须大于0");
        }

        if (dto.getTotalCount() <= 0) {
            return DeveloperResult.error("红包数量必须大于0");
        }

        if (dto.getTargetId() <= 0) {
            return DeveloperResult.error("请指定红包接收人！");
        }

        DeveloperResult<Boolean> result = receiveTargetProcessor(dto.getPaymentChannel(), dto.getTargetId(),userId,dto.getTotalCount());
        if(!result.getIsSuccessful()){
            return DeveloperResult.error(result.getMsg());
        }

        RedPacketsInfoPO redPacketsInfoPO = buildRedPacketsInfo(dto);
        redPacketsInfoRepository.save(redPacketsInfoPO);

        // 分配金额
        List<BigDecimal> distributeAmountList = this.distributeRedPacketsAmount(dto.getRedPacketsAmount(), dto.getTotalCount());
        List<RedPacketsReceiveDetailsPO> list = new ArrayList<>();
        for (BigDecimal amount : distributeAmountList) {
            list.add(RedPacketsReceiveDetailsPO.builder()
                    .redPacketsId(redPacketsInfoPO.getId())
                    .receiveUserId(dto.getPaymentChannel()==PaymentChannelEnum.FRIEND?dto.getTargetId():0)
                    .receiveAmount(amount)
                    .receiveTime(null)
                    .status(RedPacketsReceiveStatusEnum.PENDING)
                    .createTime(new Date())
                    .updateTime(new Date()).build());
        }


        redPacketsReceiveDetailsRepository.saveBatch(list);

        // 处理钱包信息
        DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(dto.getRedPacketsAmount(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.EXPENDITURE);
        if(!walletResult.getIsSuccessful()){
            return walletResult;
        }

        // 发送消息事件
        sendRedPacketsMessage(dto.getTargetId(),dto.getPaymentChannel());

        return DeveloperResult.success();
    }

    /**
     * 抢红包--要区分私发和群发
     * @param redPacketsId
     * @return
     */
    @Override
    public DeveloperResult<BigDecimal> openRedPackets(Long redPacketsId) {
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

        if(redPacketsInfo.getChannel()==PaymentChannelEnum.FRIEND){
            DeveloperResult<BigDecimal> openResult = this.openPrivateChatRedPackets(redPacketsInfo);
            if(!openResult.getIsSuccessful()){
                return openResult;
            }

            // todo 增加钱包余额
            DeveloperResult<Boolean> walletResult = walletService.doMoneyTransaction(openResult.getData(), TransactionTypeEnum.RED_PACKET, WalletOperationTypeEnum.INCOME);
            if(!walletResult.getIsSuccessful()){
                return DeveloperResult.error(walletResult.getMsg());
            }

            // 红包过期退回金额
            this.redPacketsRecoveryEvent(redPacketsId,60*60*24);

            // todo 发送红包领取提示事件给发红包发送人
            redPacketsReceiveNotifyMessage();

            return openResult;
        }

        BigDecimal openAmount;
        // 生成分布式锁的key,基于红包Id
        String lockKey = RedisKeyConstant.OPEN_RED_PACKETS_LOCK_KEY(redPacketsId);
        RLock lock = redissonClient.getLock(lockKey);
        try{
            if(lock.tryLock(30,5, TimeUnit.SECONDS)){
                List<RedPacketsReceiveDetailsPO> receiveDetailsList = redPacketsReceiveDetailsRepository.findList(redPacketsId);
                if(!receiveDetailsList.isEmpty()){
                    RedPacketsReceiveDetailsPO receiveDetails = receiveDetailsList.get(0);
                    receiveDetails.setReceiveUserId(SelfUserInfoContext.selfUserInfo().getUserId());
                    receiveDetails.setReceiveTime(new Date());
                    receiveDetails.setStatus(RedPacketsReceiveStatusEnum.SUCCESS);
                    redPacketsReceiveDetailsRepository.updateById(receiveDetails);
                    openAmount = receiveDetails.getReceiveAmount();
                }else{
                    return DeveloperResult.error("红包抢光啦！");
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

        this.redPacketsRecoveryEvent(redPacketsId,60*60*24);

        // todo 发送红包领取提示事件给发红包发送人
        redPacketsReceiveNotifyMessage();

        return DeveloperResult.success(openAmount);
    }

}
