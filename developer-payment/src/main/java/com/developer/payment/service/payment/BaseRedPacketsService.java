package com.developer.payment.service.payment;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.MessageBodyDTO;
import com.developer.framework.enums.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.TokenUtil;
import com.developer.payment.client.FriendClient;
import com.developer.payment.client.GroupClient;
import com.developer.payment.dto.FriendInfoDTO;
import com.developer.payment.dto.SelfJoinGroupInfoDTO;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BaseRedPacketsService {

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private GroupClient groupClient;

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 计算红包分配金额
     * @param totalAmount
     * @param totalCount
     * @return
     */
    public List<BigDecimal> distributeRedPacketsAmount(BigDecimal totalAmount, Integer totalCount) {
        if(totalAmount.compareTo(BigDecimal.ZERO)<=0){
            return null;
        }

        if(totalCount <=0){
            return null;
        }

        // 计算每个红包的金额，并保留两位小数
        BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.DOWN);

        // 计算剩余金额
        BigDecimal remainingAmount = totalAmount.subtract(avgAmount.multiply(BigDecimal.valueOf(totalCount)));

        List<BigDecimal> list = new ArrayList<>();

        for (int i = 0; i < totalCount; i++) {
            list.add(avgAmount);
        }

        // 分配剩余的金额到部分红包
        for (int i = 0; remainingAmount.compareTo(BigDecimal.ZERO) > 0 && i < totalCount; i++) {
            list.set(i, list.get(i).add(BigDecimal.valueOf(0.01)));
            remainingAmount = remainingAmount.subtract(BigDecimal.valueOf(0.01));
        }

        return list;
    }

    /**
     * 判断是否可以发送红包
     * @param channel
     * @param targetId
     * @return
     */
    public DeveloperResult<Boolean> receiveTargetProcessor(PaymentChannelEnum channel, Long targetId,Long userId){
        if(channel== PaymentChannelEnum.FRIEND) {
            FriendInfoDTO isFriend = friendClient.isFriend(targetId, userId).getData();
            if (isFriend==null) {
                return DeveloperResult.error("对方不是您的好友，无法发送红包！");
            }
        } else if(channel== PaymentChannelEnum.GROUP){
            List<SelfJoinGroupInfoDTO> groupList = groupClient.getSelfJoinAllGroupInfo().getData();
            Optional<SelfJoinGroupInfoDTO> optional = groupList.stream().filter(x -> x.getGroupId().equals(targetId)).findAny();
            if(!optional.isPresent()){
                return DeveloperResult.error("你不在群聊中,无法发送红包！");
            }else{
                if(optional.get().getQuit()){
                    return DeveloperResult.error("你不在群聊中,无法发送红包！");
                }
            }
        }

        return DeveloperResult.success();
    }

    /**
     * 构建红包信息
     * @return
     */
    public RedPacketsInfoPO buildRedPacketsInfo(SendRedPacketsDTO dto){
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        return RedPacketsInfoPO.builder()
                .senderUserId(userId)
                .totalCount(dto.getTotalCount())
                .remainingCount(dto.getTotalCount())
                .type(dto.getType())
                .status(RedPacketsStatusEnum.PENDING)
                .messageId(dto.getMessageId())
                .channel(dto.getPaymentChannel())
                .sendAmount(dto.getRedPacketsAmount())
                .remainingAmount(dto.getRedPacketsAmount())
                .returnAmount(BigDecimal.ZERO)
                .sendTime(new Date())
                .expireTime(DateTimeUtils.addTime(24, ChronoUnit.HOURS))
                .createTime(new Date())
                .updateTime(new Date())
                .build();
    }

    /**
     * 获取红包缓存信息
     * @param redPacketsId
     * @return
     */
    public RedPacketsInfoPO findRedPacketsCacheInfo(Long redPacketsId){
        String key = "redPackets:"+redPacketsId;
        if(redisUtil.hasKey(key)){
            return redisUtil.get(key,RedPacketsInfoPO.class);
        }

        RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(redPacketsId);
        redisUtil.set(key,redPacketsInfoPO,24, TimeUnit.HOURS);

        return redPacketsInfoPO;
    }

    public void updateRedPacketsCacheInfo(RedPacketsInfoPO redPacketsInfoPO){
        String key = "redPackets:"+redPacketsInfoPO.getId();
        redisUtil.set(key,redPacketsInfoPO,24, TimeUnit.HOURS);
    }

    public void pushSendRedPacketsMessage(){
        MessageBodyDTO<Object> dto = MessageBodyDTO.<Object>builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(null)
                .build();
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_CHAT_EXCHANGE,DeveloperMQConstant.MESSAGE_CHAT_ROUTING_KEY,dto);
    }

}
