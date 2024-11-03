package com.developer.payment.service.payment;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.TokenUtil;
import com.developer.payment.client.FriendClient;
import com.developer.payment.client.GroupClient;
import com.developer.payment.dto.FriendInfoDTO;
import com.developer.payment.dto.SelfJoinGroupInfoDTO;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.payment.dto.SendChatMessageDTO;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.utils.RabbitMQUtil;
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
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

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
    public DeveloperResult<Boolean> receiveTargetProcessor(PaymentChannelEnum channel, Long targetId,Long userId,Integer redPacketsCount){
        if(channel== PaymentChannelEnum.FRIEND) {
            if(redPacketsCount>1){
                return DeveloperResult.error("好友红包一次只能发一个");
            }

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
                if(!optional.get().getQuit()){
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
        String key = RedisKeyConstant.RED_PACKETS_INFO_KEY(redPacketsId);
        if(redisUtil.hasKey(key)){
            return redisUtil.get(key,RedPacketsInfoPO.class);
        }

        RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.getById(redPacketsId);
        redisUtil.set(key,redPacketsInfoPO,24, TimeUnit.HOURS);

        return redPacketsInfoPO;
    }

    /**
     * 更新红包缓存信息
     * @param redPacketsInfoPO
     */
    public void updateRedPacketsCacheInfo(RedPacketsInfoPO redPacketsInfoPO){
        String key = "redPackets:"+redPacketsInfoPO.getId();
        redisUtil.set(key,redPacketsInfoPO,24, TimeUnit.HOURS);
    }

    /**
     * 发送红包即时消息
     * @param targetId
     * @param channelEnum
     */
    public void sendRedPacketsMessage(Long targetId,PaymentChannelEnum channelEnum){
        MessageMainTypeEnum messageMainType = channelEnum == PaymentChannelEnum.FRIEND ? MessageMainTypeEnum.PRIVATE_MESSAGE : MessageMainTypeEnum.GROUP_MESSAGE;
        SendChatMessageDTO dto = SendChatMessageDTO.builder()
                .receiverId(targetId)
                .messageContent("红包来啦")
                .messageMainType(messageMainType)
                .messageContentType(MessageContentTypeEnum.RED_PACKETS)
                .groupId(targetId)
                .build();
        rabbitMQUtil.sendMessage(DeveloperMQConstant.MESSAGE_CHAT_EXCHANGE,DeveloperMQConstant.MESSAGE_CHAT_ROUTING_KEY, RabbitMQEventTypeEnum.MESSAGE,dto);
    }

    /**
     * 红包回收事件
     * @param redPacketsId
     * @param delayRecoveryTime
     */
    public void redPacketsRecoveryEvent(Long redPacketsId,Integer delayRecoveryTime){
        rabbitMQUtil.sendDelayMessage(DeveloperMQConstant.MESSAGE_PAYMENT_EXCHANGE,DeveloperMQConstant.MESSAGE_PAYMENT_ROUTING_KEY,RabbitMQEventTypeEnum.RED_PACKETS_RECOVERY,redPacketsId,delayRecoveryTime);
    }

    /**
     * 打开私聊红包
     * @param redPacketsInfo
     * @return
     */
    public DeveloperResult<BigDecimal> openPrivateChatRedPackets(RedPacketsInfoPO redPacketsInfo){
        RedPacketsReceiveDetailsPO detailsPO;
        List<RedPacketsReceiveDetailsPO> list = redPacketsReceiveDetailsRepository.findList(redPacketsInfo.getId());
        if(list.isEmpty()){
            // 手气红包
            detailsPO = RedPacketsReceiveDetailsPO.builder()
                    .createTime(new Date())
                    .redPacketsId(redPacketsInfo.getId())
                    .receiveTime(new Date())
                    .status(RedPacketsReceiveStatusEnum.SUCCESS)
                    .receiveAmount(redPacketsInfo.getRemainingAmount())
                    .updateTime(new Date())
                    .receiveUserId(SelfUserInfoContext.selfUserInfo().getUserId())
                    .build();
            redPacketsReceiveDetailsRepository.save(detailsPO);
        }else{
            detailsPO = list.get(0);
            detailsPO.setReceiveUserId(SelfUserInfoContext.selfUserInfo().getUserId());
            detailsPO.setReceiveTime(new Date());
            detailsPO.setReceiveAmount(redPacketsInfo.getRemainingAmount());
            detailsPO.setStatus(RedPacketsReceiveStatusEnum.SUCCESS);
            redPacketsReceiveDetailsRepository.updateById(detailsPO);
        }
        return DeveloperResult.success();
    }

    /**
     * 红包领取通知消息
     */
    public void redPacketsReceiveNotifyMessage(){
        rabbitMQUtil.sendMessage(DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, RabbitMQEventTypeEnum.IM, null);
    }
}
