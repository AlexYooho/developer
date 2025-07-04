package com.developer.payment.service.impl;

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
import com.developer.payment.pojo.SendPaymentMessageLogPO;
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
public class BasePaymentService {

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
        if (Objects.equals(totalAmount, BigDecimal.ZERO)) {
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
     * 支付基础条件判断
     *
     * @param serialNo
     * @return
     */
    public DeveloperResult<Boolean> paymentCommentConditionalJudgment(String serialNo, PaymentTypeEnum paymentTypeEnum, PaymentChannelEnum paymentChannelEnum,RedPacketsTypeEnum redPacketsTypeEnum, BigDecimal paymentAmount, Long targetId,Long userId, Integer redPacketsCount) {
        // 基础条件
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return DeveloperResult.error(serialNo, "金额最低0.01");
        }

        if (targetId <= 0) {
            return DeveloperResult.error(serialNo, "错误的接收对象");
        }

        // 红包发送条件
        if (paymentTypeEnum == PaymentTypeEnum.RED_PACKETS && paymentChannelEnum == PaymentChannelEnum.FRIEND && redPacketsCount > 1) {
            return DeveloperResult.error(serialNo, "好友红包一次只能发一个");
        }

        if (paymentTypeEnum == PaymentTypeEnum.RED_PACKETS && paymentChannelEnum == PaymentChannelEnum.FRIEND && redPacketsTypeEnum == RedPacketsTypeEnum.LUCKY) {
            return DeveloperResult.error(serialNo,"手气红包只支持群聊发送");
        }

        // 红包、转账通用条件
        if(paymentChannelEnum == PaymentChannelEnum.FRIEND) {
            FriendInfoDTO isFriend = friendClient.isFriend(IsFriendDto.builder().friendId(targetId).userId(userId).serialNo(serialNo).build()).getData();
            if (isFriend == null) {
                return DeveloperResult.error(serialNo, "对方不是您的好友");
            }
        }

        if(paymentChannelEnum == PaymentChannelEnum.GROUP){
            List<SelfJoinGroupInfoDTO> groupList = groupClient.getSelfJoinAllGroupInfo(serialNo).getData();
            Optional<SelfJoinGroupInfoDTO> optional = groupList.stream().filter(x -> x.getGroupId().equals(targetId)).findAny();
            if (!optional.isPresent()) {
                return DeveloperResult.error(serialNo, "你不在群聊中");
            } else {
                if (!optional.get().getQuit()) {
                    return DeveloperResult.error(serialNo, "你不在群聊中");
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
    public DeveloperResult<SendRedPacketsResultDTO> sendRedPacketsMessage(String serialNo, Long targetId, PaymentChannelEnum channelEnum, Long transactionId,PaymentTypeEnum paymentTypeEnum,String messageContent,MessageContentTypeEnum messageContentTypeEnum) {
        if(channelEnum == null || !channelEnum.isSupportType()){
            return DeveloperResult.error(serialNo, "消息主类型不明确");
        }

        sendRedPacketsMessageLogRepository.save(SendPaymentMessageLogPO.builder()
                .transactionId(transactionId)
                .serialNo(serialNo)
                .sendStatus(0)
                .paymentType(paymentTypeEnum)
                .createTime(new Date())
                .updateTime(new Date())
                .remark("")
                .build());

        MessageMainTypeEnum messageMainTypeEnum = channelEnum == PaymentChannelEnum.FRIEND ? MessageMainTypeEnum.PRIVATE_MESSAGE : channelEnum == PaymentChannelEnum.SCAN_CODE ? MessageMainTypeEnum.SYSTEM_MESSAGE : MessageMainTypeEnum.GROUP_MESSAGE;
        DeveloperResult<SendRedPacketsResultDTO> result = this.messageClient.sendMessage(serialNo,messageMainTypeEnum, SendMessageRequestDTO.builder()
                .serialNo(serialNo)
                .receiverId(targetId)
                .messageContent(messageContent)
                .messageMainType(messageMainTypeEnum)
                .messageContentType(messageContentTypeEnum)
                .groupId(targetId)
                .atUserIds(null)
                .referenceId(0L)
                .build());
        if (!result.getIsSuccessful()) {
            return result;
        }

        // 发送延迟检查事件,红包消息是否发送成功
        rabbitMQUtil.sendDelayMessage(serialNo, DeveloperMQConstant.MESSAGE_DELAY_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAY_ROUTING_KEY, ProcessorTypeEnum.TRANSACTION_MESSAGE_SEND_CHECK, TransactionExpiredCheckDTO.builder().serialNo(serialNo).paymentTypeEnum(paymentTypeEnum).transactionId(transactionId).build(), 30);
        return result;
    }

    /**
     * 交易过期检查资金退回事件
     *
     * @param transactionId
     * @param expireTime
     */
    public void transactionExpiredCheckEvent(String serialNo,PaymentTypeEnum paymentTypeEnum, Long transactionId, Long expireTime) {
        Integer expiredSeconds = Math.toIntExact((expireTime - new Date().getTime()) / 1000);
        rabbitMQUtil.sendDelayMessage(serialNo, DeveloperMQConstant.MESSAGE_DELAY_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAY_ROUTING_KEY, ProcessorTypeEnum.TRANSACTION_EXPIRED_CHECK, TransactionExpiredCheckDTO.builder().serialNo(serialNo).paymentTypeEnum(paymentTypeEnum).transactionId(transactionId).build(), expiredSeconds);
    }

    /**
     * 打开私聊红包
     *
     * @param redPacketsInfo
     * @return
     */
    public BigDecimal openPrivateChatRedPackets(RedPacketsInfoPO redPacketsInfo) {
        BigDecimal amount = this.distributeRedPacketsAmount(redPacketsInfo.getRemainingAmount(), redPacketsInfo.getRemainingCount());
        if (Objects.equals(amount, BigDecimal.ZERO)) {
            return amount;
        }

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
