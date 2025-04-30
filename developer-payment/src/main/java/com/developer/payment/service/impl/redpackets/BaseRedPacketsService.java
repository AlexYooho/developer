package com.developer.payment.service.impl.redpackets;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.payment.client.FriendClient;
import com.developer.payment.client.GroupClient;
import com.developer.payment.client.MessageClient;
import com.developer.payment.dto.*;
import com.developer.framework.dto.SendRedPacketsDTO;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.enums.RedPacketsStatusEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import com.developer.payment.pojo.SendRedPacketsMessageLogPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.repository.RedPacketsReceiveDetailsRepository;
import com.developer.payment.repository.SendRedPacketsMessageLogRepository;
import com.developer.payment.utils.RabbitMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class BaseRedPacketsService {

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private GroupClient groupClient;

    @Autowired
    private MessageClient messageClient;

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedPacketsReceiveDetailsRepository redPacketsReceiveDetailsRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Autowired
    private SendRedPacketsMessageLogRepository sendRedPacketsMessageLogRepository;

    /**
     * 计算红包分配金额--平均分配
     *
     * @param totalAmount
     * @param totalCount
     * @return
     */
    public BigDecimal distributeRedPacketsAmount(BigDecimal totalAmount, Integer totalCount) {
        List<BigDecimal> list = new ArrayList<>();
        if(Objects.equals(totalAmount, BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }

        if (totalCount <= 0) {
            return BigDecimal.ZERO;
        }

        if (totalCount == 1) {
            list.add(totalAmount);
            return totalAmount;
        }

        // 计算每个红包的金额，并保留两位小数
        BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.DOWN);

        for (int i = 0; i < totalCount; i++) {
            list.add(avgAmount);
        }

        // 计算剩余金额
        BigDecimal remainingAmount = totalAmount.subtract(avgAmount.multiply(BigDecimal.valueOf(totalCount)));

        // 分配剩余的金额到部分红包
        for (int i = 0; remainingAmount.compareTo(BigDecimal.ZERO) > 0 && i < totalCount; i++) {
            list.set(i, list.get(i).add(BigDecimal.valueOf(0.01)));
            remainingAmount = remainingAmount.subtract(BigDecimal.valueOf(0.01));
        }

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * 判断是否可以发送红包
     *
     * @param channel
     * @param targetId
     * @return
     */
    public DeveloperResult<Boolean> sendConditionalJudgment(String serialNo, RedPacketsTypeEnum redPacketsType, PaymentChannelEnum channel, Long targetId, Long userId, Integer redPacketsCount, BigDecimal redPacketsAmount) {
        if (redPacketsAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return DeveloperResult.error(serialNo, "红包金额必须大于0");
        }

        if (redPacketsCount <= 0) {
            return DeveloperResult.error(serialNo, "红包数量必须大于0");
        }

        if (targetId <= 0) {
            return DeveloperResult.error(serialNo, "请指定红包发送目标");
        }

        if (channel == PaymentChannelEnum.FRIEND) {
            if (redPacketsCount > 1) {
                return DeveloperResult.error(serialNo, "好友红包一次只能发一个");
            }

            if (redPacketsType == RedPacketsTypeEnum.LUCKY) {
                return DeveloperResult.error(serialNo, "手气红包只支持群组发送");
            }

            FriendInfoDTO isFriend = friendClient.isFriend(IsFriendDto.builder().friendId(targetId).userId(userId).serialNo(serialNo).build()).getData();
            if (isFriend == null) {
                return DeveloperResult.error(serialNo, "对方不是您的好友，无法发送红包！");
            }
        } else if (channel == PaymentChannelEnum.GROUP) {
            List<SelfJoinGroupInfoDTO> groupList = groupClient.getSelfJoinAllGroupInfo(serialNo).getData();
            Optional<SelfJoinGroupInfoDTO> optional = groupList.stream().filter(x -> x.getGroupId().equals(targetId)).findAny();
            if (!optional.isPresent()) {
                return DeveloperResult.error(serialNo, "你不在群聊中,无法发送红包！");
            } else {
                if (!optional.get().getQuit()) {
                    return DeveloperResult.error(serialNo, "你不在群聊中,无法发送红包！");
                }
            }
        }

        return DeveloperResult.success(serialNo);
    }

    /**
     * 构建红包信息
     *
     * @return
     */
    public RedPacketsInfoPO buildRedPacketsInfo(SendRedPacketsDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        RedPacketsInfoPO model = RedPacketsInfoPO.builder()
                .senderUserId(userId)
                .totalCount(dto.getTotalCount())
                .remainingCount(dto.getTotalCount())
                .receiveTargetId(dto.getTargetId())
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
        return model;
    }

    /**
     * 获取红包缓存信息
     *
     * @param redPacketsId
     * @return
     */
    public RedPacketsInfoPO findRedPacketsCacheInfo(Long redPacketsId) {
        String key = RedisKeyConstant.RED_PACKETS_INFO_KEY(redPacketsId);
        if (redisUtil.hasKey(key)) {
            return redisUtil.get(key, RedPacketsInfoPO.class);
        }

        RedPacketsInfoPO redPacketsInfoPO = redPacketsInfoRepository.findById(redPacketsId);
        redisUtil.set(key, redPacketsInfoPO, 24, TimeUnit.HOURS);

        return redPacketsInfoPO;
    }

    /**
     * 更新红包缓存信息
     *
     * @param redPacketsInfoPO
     */
    public void updateRedPacketsCacheInfo(RedPacketsInfoPO redPacketsInfoPO) {
        String key = RedisKeyConstant.RED_PACKETS_INFO_KEY(redPacketsInfoPO.getId());
        redisUtil.set(key, redPacketsInfoPO, 24, TimeUnit.HOURS);
    }

    /**
     * 发送红包即时消息
     *
     * @param targetId
     * @param channelEnum
     */
    public DeveloperResult sendRedPacketsMessage(String serialNo, Long targetId, PaymentChannelEnum channelEnum, Long redPacketsId) {
        if (channelEnum != PaymentChannelEnum.FRIEND && channelEnum != PaymentChannelEnum.GROUP) {
            return DeveloperResult.error(serialNo, "消息主类型不明确");
        }

        SendRedPacketsMessageLogPO log = SendRedPacketsMessageLogPO.builder()
                .redPacketsId(redPacketsId)
                .serialNo(serialNo)
                .sendStatus(0)
                .createTime(new Date())
                .updateTime(new Date())
                .remark("")
                .build();
        sendRedPacketsMessageLogRepository.save(log);

        MessageMainTypeEnum messageMainTypeEnum = channelEnum == PaymentChannelEnum.FRIEND ? MessageMainTypeEnum.PRIVATE_MESSAGE : MessageMainTypeEnum.GROUP_MESSAGE;
        SendMessageRequestDTO sendMessageRequest = SendMessageRequestDTO.builder()
                .serialNo(serialNo)
                .receiverId(targetId)
                .messageContent("红包来啦")
                .messageMainType(messageMainTypeEnum)
                .messageContentType(MessageContentTypeEnum.RED_PACKETS)
                .groupId(0L)
                .atUserIds(null)
                .referenceId(0L)
                .build();
        DeveloperResult result = this.messageClient.sendMessage(messageMainTypeEnum, sendMessageRequest);

        // 发送延迟检查事件,红包消息是否发送成功
        rabbitMQUtil.sendDelayMessage(serialNo, DeveloperMQConstant.MESSAGE_DELAY_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAY_ROUTING_KEY, ProcessorTypeEnum.RED_PACKETS_MESSAGE_SEND_CHECK, redPacketsId, 30);
        return result;
    }

    /**
     * 红包回收事件
     *
     * @param redPacketsId
     * @param expireTime
     */
    public void redPacketsRecoveryEvent(String serialNo, Long redPacketsId, Long expireTime) {
        Integer redPacketExpireSeconds = Math.toIntExact((expireTime - new Date().getTime()) / 1000);
        rabbitMQUtil.sendDelayMessage(serialNo, DeveloperMQConstant.MESSAGE_DELAY_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAY_ROUTING_KEY, ProcessorTypeEnum.RED_PACKETS_RETURN, redPacketsId, redPacketExpireSeconds);
    }

    /**
     * 打开私聊红包
     *
     * @param redPacketsInfo
     * @return
     */
    public BigDecimal openPrivateChatRedPackets(RedPacketsInfoPO redPacketsInfo) {
        BigDecimal amount = this.distributeRedPacketsAmount(redPacketsInfo.getRemainingAmount(), redPacketsInfo.getRemainingCount());
        RedPacketsReceiveDetailsPO detailsPO = RedPacketsReceiveDetailsPO.builder()
                .redPacketsId(redPacketsInfo.getId())
                .receiveUserId(SelfUserInfoContext.selfUserInfo().getUserId())
                .receiveAmount(amount)
                .receiveTime(new Date())
                .status(RedPacketsReceiveStatusEnum.SUCCESS)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        redPacketsReceiveDetailsRepository.save(detailsPO);

        // 处理红包主表
        redPacketsInfo.setUpdateTime(new Date());
        redPacketsInfo.setReturnAmount(BigDecimal.ZERO);
        redPacketsInfo.setRemainingAmount(BigDecimal.ZERO);
        redPacketsInfo.setStatus(RedPacketsStatusEnum.FINISHED);
        redPacketsInfo.setRemainingCount(0);
        redPacketsInfoRepository.updateById(redPacketsInfo);
        return detailsPO.getReceiveAmount();
    }

    /**
     * 红包领取通知消息
     */
    public void redPacketsReceiveNotifyMessage(String serialNo, Long targetId, PaymentChannelEnum channelEnum) {
        SendChatMessageDTO dto = SendChatMessageDTO.builder()
                .receiverId(targetId)
                .messageContent(SelfUserInfoContext.selfUserInfo().getNickName() + "领取了你的红包")
                .messageMainType(channelEnum == PaymentChannelEnum.FRIEND ? MessageMainTypeEnum.PRIVATE_MESSAGE : MessageMainTypeEnum.GROUP_MESSAGE)
                .messageContentType(MessageContentTypeEnum.TEXT)
                .groupId(targetId)
                .build();
        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_CHAT_EXCHANGE, DeveloperMQConstant.MESSAGE_CHAT_ROUTING_KEY, ProcessorTypeEnum.MESSAGE, dto);
    }
}
