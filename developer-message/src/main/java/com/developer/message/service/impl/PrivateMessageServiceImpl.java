package com.developer.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.*;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.client.PaymentClient;
import com.developer.message.dto.*;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.FriendService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends AbstractMessageAdapterService {

    private final RedisUtil redisUtil;
    private final RabbitMQUtil rabbitMQUtil;
    private final FriendService friendService;
    private final PaymentClient paymentClient;
    private final MessageLikeService messageLikeService;
    private final PrivateMessageRepository privateMessageRepository;
    private final RpcClient rpcClient;

    /**
     * 消息主体类型
     *
     * @return
     */
    @Override
    public MessageMainTypeEnum messageMainType() {
        return MessageMainTypeEnum.PRIVATE_MESSAGE;
    }

    /*
     * 拉取最新消息--这里需要改成按会话id获取
     */
    @Override
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req) {
        List<LoadMessageListResponseDTO> list = new ArrayList<>();

        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());

        // 判断当前聊天会话是否有新的消息
        // 和当前对象会话的maxSeq
        String maxSeqKey = RedisKeyConstant.CURRENT_CONVERSATION_MAX_SEQ_KEY(uidA, uidB);
        Long maxSeq = Optional.ofNullable(redisUtil.get(maxSeqKey, Long.class)).orElse(0L);
        // 当前设备终端最大的convSeq
        String lastSeqKey = RedisKeyConstant.CURRENT_TERMINAL_LAST_SEQ_KEY(uidA, uidB, req.getTerminalType().code());
        Long lastSeq = Optional.ofNullable(redisUtil.get(lastSeqKey, Long.class)).orElse(0L);
        if (maxSeq > 0 && lastSeq > 0) {
            if (maxSeq.equals(lastSeq)) {
                return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
            }
        }

        // 获取当前用户的最新聊天消息
        List<PrivateMessagePO> messages = privateMessageRepository.getMessageListByUserId(lastSeq, uidA, uidB);

        // 将所有消息改为已读状态
        List<Long> ids = messages.stream()
                .filter(x -> !x.getSendId().equals(SelfUserInfoContext.selfUserInfo().getUserId()))
                .map(PrivateMessagePO::getId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(ids)) {
            privateMessageRepository.updateMessageStatus(ids, MessageStatusEnum.READED);

            // 修改当前终端的lastSeq
            redisUtil.set(lastSeqKey, Collections.max(ids));
        }

        // 聚合返回数据
        list = messages.stream().map(x -> {
            LoadMessageListResponseDTO dto = new LoadMessageListResponseDTO();
            dto.setId(x.getId());
            dto.setSendId(x.getSendId());
            dto.setReceiverId(x.getReceiverId());
            dto.setConvSeq(x.getConvSeq());
            dto.setMessageContent(x.getMessageContent());
            dto.setMessageContentType(x.getMessageContentType());
            dto.setMessageStatus(x.getMessageStatus());
            dto.setReadStatus(x.getReadStatus());
            dto.setSendNickName("");
            dto.setSendTime(x.getSendTime());
            dto.setReferenceId(x.getReferenceId());
            dto.setLikeCount(x.getLikeCount());
            return dto;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    /*
     * 发送消息
     */
    @Transactional
    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();

        // 不能给自己发送消息
        if (userId.equals(req.getReceiverId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不能给自己发送消息");
        }

        // 好友关系校验
        DeveloperResult<Boolean> friend = friendService.isFriend(userId, req.getReceiverId());
        if (!friend.getIsSuccessful() || !friend.getData()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "您还不是对方的好友");
        }

        // 发送者、接收者id
        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getReceiverId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getReceiverId());

        // 消息入库
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setUidA(uidA);
        privateMessage.setUidB(uidB);
        privateMessage.setSendId(userId);
        privateMessage.setReceiverId(req.getReceiverId());
        privateMessage.setConvSeq(getCurrentConversationNextConvSeq(uidA, uidB));
        privateMessage.setClientMsgId(req.getClientMsgId());
        privateMessage.setMessageContent(req.getMessageContent());
        privateMessage.setMessageContentType(req.getMessageContentType());
        privateMessage.setMessageStatus(MessageStatusEnum.UNSEND);
        privateMessage.setReadStatus(0);
        privateMessage.setSendTime(new Date());
        privateMessage.setReferenceId(req.getReferenceId());
        privateMessage.setLikeCount(0L);
        privateMessage.setExtra("");
        privateMessage.setDeleted(false);
        privateMessage.setCreateTime(new Date());
        privateMessage.setUpdateTime(new Date());
        privateMessageRepository.save(privateMessage);

        // 红包转账消息调用支付接口
        if (req.getMessageContentType().equals(MessageContentTypeEnum.TRANSFER)
                || req.getMessageContentType().equals(MessageContentTypeEnum.RED_PACKETS)) {
            InvokeRedPacketsTransferRequestRpcDTO paymentDto = new InvokeRedPacketsTransferRequestRpcDTO();
            paymentDto.setPaymentType(req.getPaymentInfoDTO().getPaymentType());
            paymentDto.setPaymentAmount(req.getPaymentInfoDTO().getPaymentAmount());
            paymentDto.setTargetId(req.getReceiverId());
            paymentDto.setRedPacketsTotalCount(req.getPaymentInfoDTO().getRedPacketsTotalCount());
            paymentDto.setRedPacketsType(req.getPaymentInfoDTO().getRedPacketsType());
            paymentDto.setMessageId(privateMessage.getId());
            paymentDto.setPaymentChannel(PaymentChannelEnum.FRIEND);
            DeveloperResult<Boolean> execute = RpcExecutor
                    .execute(() -> rpcClient.paymentRpcService.invokeRedPacketsTransfer(paymentDto));
            if (!execute.getIsSuccessful()) {
                return DeveloperResult.error(SerialNoHolder.getSerialNo(), execute.getMsg());
            }
        }

        // 发送消息
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(req.getMessageMainType(), req.getMessageContentType(),
                        privateMessage.getMessageStatus(), req.getTerminalType(), privateMessage.getId(), userId,
                        nickName,
                        req.getMessageContent(), privateMessage.getSendTime(), req.getReceiverId()));

        // 更新当前聊天会话maxSeq
        String maxSeqKey = RedisKeyConstant.CURRENT_CONVERSATION_MAX_SEQ_KEY(uidA, uidB);
        redisUtil.set(maxSeqKey, privateMessage.getConvSeq());

        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(privateMessage.getId());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), dto);
    }

    /*
     * 已读消息
     */
    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT,
                        MessageStatusEnum.READED, TerminalTypeEnum.WEB, 0L, userId, nickName, "", new Date(),
                        req.getTargetId()));
        privateMessageRepository.updateMessageStatus(req.getTargetId(), userId, MessageStatusEnum.READED.code());
        return DeveloperResult.success(serialNo);
    }

    /*
     * 撤回消息
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(RecallMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        PrivateMessagePO privateMessage = privateMessageRepository.getById(req.getMessageId());
        if (privateMessage == null) {
            return DeveloperResult.error(serialNo, "消息不存在");
        }

        if (!privateMessage.getSendId().equals(userId)) {
            return DeveloperResult.error(serialNo, "该消息不是你发送的,无法撤回");
        }

        if (System.currentTimeMillis() - privateMessage.getSendTime().getTime() > DeveloperConstant.ALLOW_RECALL_SECOND
                * 1000) {
            return DeveloperResult.error(serialNo, "消息发送已超过一定时间,无法撤回");
        }

        // 修改消息状态
        privateMessage.setMessageStatus(MessageStatusEnum.RECALL);
        privateMessageRepository.updateById(privateMessage);

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT,
                        MessageStatusEnum.RECALL, TerminalTypeEnum.WEB, privateMessage.getId(), userId, nickName,
                        "对方撤回了一条消息", new Date(), privateMessage.getReceiverId()));

        return DeveloperResult.success(serialNo);
    }

    /*
     * 获取历史消息
     */
    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage() > 0 ? req.getPage() : 1);
        req.setSize(req.getSize() > 0 ? req.getSize() : 10);
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        long pageIndex = (req.getPage() - 1) * req.getSize();
        List<PrivateMessagePO> list = privateMessageRepository.getHistoryMessageList(userId, req.getTargetId(),
                pageIndex, req.getSize());
        List<SendMessageResultDTO> collect = list.stream()
                .map(a -> BeanUtils.copyProperties(a, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo, collect);
    }

    /*
     * 新增消息
     */
    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();

        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), dto.getReceiverId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), dto.getReceiverId());
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setUidA(uidA);
        privateMessage.setUidB(uidB);
        privateMessage.setSendId(dto.getSendId());
        privateMessage.setReceiverId(dto.getReceiverId());
        privateMessage.setConvSeq(getCurrentConversationNextConvSeq(uidA, uidB));
        privateMessage.setClientMsgId("");
        privateMessage.setMessageContent(dto.getMessageContent());
        privateMessage.setMessageContentType(dto.getMessageContentType());
        privateMessage.setMessageStatus(MessageStatusEnum.UNSEND);
        privateMessage.setReadStatus(0);
        privateMessage.setSendTime(new Date());
        privateMessage.setReferenceId(0L);
        privateMessage.setLikeCount(0L);
        privateMessage.setExtra("");
        privateMessage.setDeleted(false);
        privateMessage.setCreateTime(new Date());
        privateMessage.setUpdateTime(new Date());
        boolean isSuccess = privateMessageRepository.save(privateMessage);
        return DeveloperResult.success(serialNo, isSuccess);
    }

    /*
     * 删除消息
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        boolean isSuccess = privateMessageRepository.deleteChatMessage(userId, req.getTargetId());
        return DeveloperResult.success(serialNo, isSuccess);
    }

    /*
     * 回复消息
     */
    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(id);
        String serialNo = SerialNoHolder.getSerialNo();
        if (messagePO == null) {
            return DeveloperResult.error(serialNo, "回复消息不存在");
        }

        req.setReferenceId(id);
        this.sendMessage(SendMessageRequestDTO.builder().serialNo(serialNo).receiverId(req.getReceiverId())
                .messageContent(req.getMessageContent())
                .messageMainType(req.getMessageMainType()).messageContentType(req.getMessageContentType())
                .groupId(req.getGroupId()).atUserIds(req.getAtUserIds())
                .referenceId(id).build());
        return DeveloperResult.success(serialNo);
    }

    /*
     * 收藏
     */
    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    /*
     * 转发消息
     */
    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(req.getMessageId());
        String serialNo = SerialNoHolder.getSerialNo();
        if (messagePO == null) {
            return DeveloperResult.error(serialNo, "转发消息本体不存在");
        }

        for (Long userId : req.getUserIdList()) {
            SendMessageRequestDTO dto = SendMessageRequestDTO.builder()
                    .serialNo(serialNo)
                    .messageContent(messagePO.getMessageContent())
                    .receiverId(userId)
                    .messageContentType(messagePO.getMessageContentType())
                    .messageMainType(MessageMainTypeEnum.PRIVATE_MESSAGE)
                    .build();

            this.sendMessage(dto);
        }
        return DeveloperResult.success(serialNo);
    }

    /*
     * 点赞消息
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageMainTypeEnum.PRIVATE_MESSAGE);
    }

    /*
     * 取消点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageMainTypeEnum.PRIVATE_MESSAGE);
    }

    /*
     * 是否支付类型消息
     */
    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return messageContentTypeEnum == MessageContentTypeEnum.RED_PACKETS
                || messageContentTypeEnum == MessageContentTypeEnum.TRANSFER;
    }

    /*
     * 构建mq消息dto
     */
    private ChatMessageDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum,
            MessageContentTypeEnum messageContentTypeEnum, MessageStatusEnum messageStatus,
            TerminalTypeEnum terminalType, Long messageId, Long sendId, String sendNickName,
            String messageContent, Date sendTime, Long friendId) {
        return ChatMessageDTO
                .builder()
                .messageMainTypeEnum(messageMainTypeEnum)
                .messageContentTypeEnum(messageContentTypeEnum)
                .messageStatus(messageStatus)
                .terminalType(terminalType)
                .messageId(messageId)
                .sendId(sendId)
                .sendNickName(sendNickName)
                .messageContent(messageContent)
                .sendTime(sendTime)
                .friendUserId(friendId)
                .build();
    }

    private RabbitMQMessageBodyDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum,
            MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId,
            String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds,
            MessageStatusEnum messageStatus, TerminalTypeEnum terminalType, Date sendTime) {
        return RabbitMQMessageBodyDTO.builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .data(ChatMessageDTO.builder().messageMainTypeEnum(messageMainTypeEnum)
                        .messageContentTypeEnum(messageContentTypeEnum)
                        .messageId(messageId)
                        .groupId(groupId)
                        .sendId(sendId)
                        .sendNickName(sendNickName)
                        .messageContent(messageContent)
                        .receiverIds(receiverIds)
                        .atUserIds(atUserIds)
                        .messageStatus(messageStatus)
                        .terminalType(terminalType)
                        .sendTime(sendTime).build())
                .build();
    }

    /*
     * 好友申请接受消息
     */
    @Override
    public DeveloperResult<Boolean> friendApplyAcceptMessage(Long receiverId) {
        // 新增消息记录
        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), receiverId);
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), receiverId);
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setUidA(uidA);
        privateMessage.setUidB(uidB);
        privateMessage.setSendId(SelfUserInfoContext.selfUserInfo().getUserId());
        privateMessage.setReceiverId(receiverId);
        privateMessage.setConvSeq(getCurrentConversationNextConvSeq(uidA, uidB));
        privateMessage.setClientMsgId("");
        privateMessage.setMessageContent("我们已经是好友啦");
        privateMessage.setMessageContentType(MessageContentTypeEnum.TEXT);
        privateMessage.setMessageStatus(MessageStatusEnum.UNSEND);
        privateMessage.setReadStatus(0);
        privateMessage.setSendTime(new Date());
        privateMessage.setReferenceId(0L);
        privateMessage.setLikeCount(0L);
        privateMessage.setExtra("");
        privateMessage.setDeleted(false);
        privateMessage.setCreateTime(new Date());
        privateMessage.setUpdateTime(new Date());
        privateMessageRepository.save(privateMessage);

        // 推送im消息
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L,
                        SelfUserInfoContext.selfUserInfo().getUserId(),
                        SelfUserInfoContext.selfUserInfo().getNickName(), privateMessage.getMessageContent(),
                        Collections.singletonList(receiverId), new ArrayList<>(), MessageStatusEnum.UNSEND,
                        TerminalTypeEnum.WEB, new Date()));

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 好友申请拒绝消息
     */
    @Override
    public DeveloperResult<Boolean> friendApplyRejectMessage(Long receiverId, String rejectReason) {

        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L,
                        SelfUserInfoContext.selfUserInfo().getUserId(),
                        SelfUserInfoContext.selfUserInfo().getNickName(), rejectReason,
                        Collections.singletonList(receiverId), new ArrayList<>(), MessageStatusEnum.UNSEND,
                        TerminalTypeEnum.WEB, new Date()));

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 发送加入群聊邀请消息
     */
    @Override
    public DeveloperResult<Boolean> sendJoinGroupInviteMessage(List<Long> memberIds, String groupName,
            String inviterName, String groupAvatar) {

        for (Long memberId : memberIds) {
            String content = "邀请你加入群聊,".concat(inviterName).concat("邀请你加入群聊").concat(groupName).concat("进入可查看详情")
                    .concat(groupAvatar);
            rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(),
                    DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                    DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY,
                    ProcessorTypeEnum.IM,
                    builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE,
                            MessageContentTypeEnum.GROUP_INVITE,
                            0L,
                            0L,
                            SelfUserInfoContext.selfUserInfo().getUserId(),
                            SelfUserInfoContext.selfUserInfo().getNickName(),
                            content,
                            Collections.singletonList(memberId),
                            new ArrayList<>(),
                            MessageStatusEnum.UNSEND,
                            TerminalTypeEnum.WEB,
                            new Date()));
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    private long getCurrentConversationNextConvSeq(Long uidA, Long uidB) {
        String key = RedisKeyConstant.CURRENT_CONVERSATION_NEXT_CONV_SEQ_KEY(uidA, uidB);
        return redisUtil.increment(key, 1L);
    }
}
