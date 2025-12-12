package com.developer.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.*;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.*;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.ConversationService;
import com.developer.message.service.FriendService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends AbstractMessageAdapterService {

    private final RedisUtil redisUtil;
    private final RabbitMQUtil rabbitMQUtil;
    private final FriendService friendService;
    private final MessageLikeService messageLikeService;
    private final PrivateMessageRepository privateMessageRepository;
    private final RpcClient rpcClient;
    private final ConversationService conversationService;

    /**
     * 消息主体类型
     *
     * @return
     */
    @Override
    public MessageConversationTypeEnum messageMainType() {
        return MessageConversationTypeEnum.PRIVATE_MESSAGE;
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
        String maxSeqKey = RedisKeyConstant.CURRENT_CONVERSATION_MAX_SEQ_KEY(uidA.toString(), uidB.toString());
        Long maxSeq = Optional.ofNullable(redisUtil.get(maxSeqKey, Long.class)).orElse(0L);
        // 当前设备终端最大的convSeq
        String lastSeqKey = RedisKeyConstant.CURRENT_TERMINAL_LAST_SEQ_KEY(uidA.toString(), uidB.toString(), req.getTerminalType().code());
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
        if (userId.equals(req.getTargetId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不能给自己发送消息");
        }

        // 好友关系校验
        DeveloperResult<Boolean> friend = friendService.isFriend(userId, req.getTargetId());
        if (!friend.getIsSuccessful() || !friend.getData()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "您还不是对方的好友");
        }

        // 发送者、接收者id
        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());

        // 消息入库
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setUidA(uidA);
        privateMessage.setUidB(uidB);
        privateMessage.setSendId(userId);
        privateMessage.setReceiverId(req.getTargetId());
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
        privateMessage.setVisibleToOneself(true);
        privateMessage.setCreateTime(new Date());
        privateMessage.setUpdateTime(new Date());
        privateMessageRepository.save(privateMessage);

        // 红包转账消息调用支付接口
        DeveloperResult<Boolean> invokedPayResult = invokePay(privateMessage.getId(), rpcClient, req);
        if(!invokedPayResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),invokedPayResult.getMsg());
        }

        // 维护会话列表
        UpsertConversationRequestDTO conversationRequestDTO = buildUpsertConversationRequestDTO(req, privateMessage);
        DeveloperResult<Boolean> conversationResult = conversationService.upsertCurrentConversation(conversationRequestDTO);
        if (!conversationResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), conversationResult.getMsg());
        }

        // 发送消息--会话类型、消息内容类型、消息内容、目标对象、发送者信息、发送终端类型、发送时间
        ChatMessageDTO chatMessageDTO = builderMQMessageDTO(req.getMessageMainType(), req.getMessageContentType(),
                privateMessage.getMessageStatus(), req.getTerminalType(), privateMessage.getId(), userId,nickName,
                req.getMessageContent(), privateMessage.getSendTime(), req.getTargetId());
        rabbitMQUtil.sendChatMessage(chatMessageDTO);

        // 更新当前聊天会话maxSeq
        String maxSeqKey = RedisKeyConstant.CURRENT_CONVERSATION_MAX_SEQ_KEY(uidA.toString(), uidB.toString());
        redisUtil.set(maxSeqKey, privateMessage.getConvSeq());

        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(privateMessage.getId());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), dto);
    }

    /*
    新增修改会话参数DTO
     */
    private static UpsertConversationRequestDTO buildUpsertConversationRequestDTO(SendMessageRequestDTO req, PrivateMessagePO privateMessage) {
        UpsertConversationRequestDTO conversationRequestDTO = new UpsertConversationRequestDTO();
        conversationRequestDTO.setTargetId(req.getTargetId());
        conversationRequestDTO.setLastMsgSeq(privateMessage.getConvSeq());
        conversationRequestDTO.setLastMsgId(privateMessage.getId());
        conversationRequestDTO.setLastMsgContent(req.getMessageContent());
        conversationRequestDTO.setLastMsgType(req.getMessageContentType());
        conversationRequestDTO.setLastMsgTime(new Date());
        return conversationRequestDTO;
    }

    /*
     * 已读消息
     */
    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        // 校验当前聊天会话是否有未读消息
        List<ChatConversationListResponseDTO> conversationList = conversationService.findChatConversationList().getData();
        if (CollUtil.isEmpty(conversationList)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不存在会话内容");
        }
        boolean existUnread = conversationList.stream().anyMatch(x -> x.getTargetId().equals(req.getTargetId()) && x.getUnreadCount() > 0);
        if (!existUnread) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "当前会话不存在未读消息");
        }

        // 再去校验和当前用户的聊天记录是否存在未读的
        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        List<PrivateMessagePO> messageList = privateMessageRepository.findMessageByStatus(uidA, uidB, MessageStatusEnum.SENDED);
        if (CollUtil.isEmpty(messageList)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "当前会话不存在未读消息");
        }

        // 修改会话列表
        UpsertConversationRequestDTO conversationRequestDTO = new UpsertConversationRequestDTO();
        conversationRequestDTO.setTargetId(req.getTargetId());
        conversationRequestDTO.setUnreadCount(0);
        DeveloperResult<Boolean> updateConversationResult = conversationService.upsertCurrentConversation(conversationRequestDTO);
        if(!updateConversationResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), updateConversationResult.getMsg());
        }

        // 通知消息发送者已读
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageConversationTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT,
                        MessageStatusEnum.READED, TerminalTypeEnum.WEB, 0L, SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), "", new Date(),
                        req.getTargetId()));

        // 修改消息状态为已读状态
        privateMessageRepository.updateMessageStatus(messageList.stream().map(PrivateMessagePO::getId).collect(Collectors.toList()), MessageStatusEnum.READED);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 撤回消息
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(WithdrawMessageRequestDTO req) {

        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());

        // 检验消息是否存在
        PrivateMessagePO privateMessage = privateMessageRepository.findMessageByMessageId(uidA, uidB, req.getMessageId());
        if (privateMessage == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "消息不存在");
        }

        // 是否重复操作
        if (privateMessage.getMessageStatus().equals(MessageStatusEnum.RECALL)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不能重复操作");
        }

        // 校验发送者
        if (!privateMessage.getSendId().equals(SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "该消息不是你发送的,无法撤回");
        }

        // 校验撤回时间限制
        if (System.currentTimeMillis() - privateMessage.getSendTime().getTime() > DeveloperConstant.ALLOW_RECALL_SECOND * 1000) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "消息发送已超过一定时间,无法撤回");
        }

        // 修改消息状态
        privateMessageRepository.updateMessageStatus(Collections.singletonList(privateMessage.getId()), MessageStatusEnum.RECALL);

        // 通知撤回
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageConversationTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT,
                        MessageStatusEnum.RECALL, TerminalTypeEnum.WEB, privateMessage.getId(), SelfUserInfoContext.selfUserInfo().getUserId(),
                        SelfUserInfoContext.selfUserInfo().getNickName(), "对方撤回了一条消息", new Date(), privateMessage.getReceiverId()));

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 获取历史消息
     */
    @Override
    public DeveloperResult<List<QueryHistoryMessageResponseDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage() > 0 ? req.getPage() : 1);
        req.setSize(req.getSize() > 0 ? req.getSize() : 10);
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        long pageIndex = (req.getPage() - 1) * req.getSize();
        List<PrivateMessagePO> list = privateMessageRepository.getHistoryMessageList(userId, req.getTargetId(), pageIndex, req.getSize());
        List<QueryHistoryMessageResponseDTO> collect = list.stream().map(a -> BeanUtils.copyProperties(a, QueryHistoryMessageResponseDTO.class)).collect(Collectors.toList());
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
        privateMessage.setVisibleToOneself(true);
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
        Long uidA = Math.min(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());
        Long uidB = Math.max(SelfUserInfoContext.selfUserInfo().getUserId(), req.getTargetId());

        // 是否删除与目标用户的所有聊天记录
        List<Long> messageIds = new ArrayList<>();
        if (req.getAll()) {
            List<PrivateMessagePO> messages = privateMessageRepository.findAllMessageByTarget(uidA,uidB);
            if(CollUtil.isNotEmpty(messages)){
                messageIds = messages.stream().map(PrivateMessagePO::getId).collect(Collectors.toList());
            }
        } else {
            PrivateMessagePO message = privateMessageRepository.findMessageByMessageId(uidA,uidB, req.getMessageId());
            if(ObjectUtil.isNotEmpty(message)){
                messageIds.add(message.getId());
            }
        }

        // 判断是否有消息需要移除
        if(CollUtil.isEmpty(messageIds)){
            return DeveloperResult.error("没有需要删除的消息");
        }

        // 修改消息的删除状态--是否对自己可见
        privateMessageRepository.updateDeleteStatus(messageIds,false);
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 回复消息
     */
    @Transactional
    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.findMessageByMessageId(id);
        if (ObjectUtil.isEmpty(messagePO)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "回复消息不存在");
        }

        SendMessageRequestDTO dto = SendMessageRequestDTO.builder()
                .targetId(req.getReceiverId())
                .messageContent(req.getMessageContent())
                .messageMainType(req.getMessageMainType())
                .messageContentType(req.getMessageContentType())
                .atUserIds(req.getAtUserIds())
                .referenceId(id)
                .build();

        sendMessage(dto);
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
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
    @Transactional
    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.findMessageByMessageId(req.getMessageId());
        if (messagePO == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "转发消息本体不存在");
        }

        for (Long userId : req.getUserIdList()) {
            SendMessageRequestDTO dto = SendMessageRequestDTO.builder()
                    .messageContent(messagePO.getMessageContent())
                    .targetId(userId)
                    .messageContentType(messagePO.getMessageContentType())
                    .messageMainType(MessageConversationTypeEnum.PRIVATE_MESSAGE)
                    .build();

            sendMessage(dto);
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
     * 点赞消息
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageConversationTypeEnum.PRIVATE_MESSAGE);
    }

    /*
     * 取消点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageConversationTypeEnum.PRIVATE_MESSAGE);
    }

    /*
     * 构建mq消息dto
     */
    private ChatMessageDTO builderMQMessageDTO(MessageConversationTypeEnum messageConversationTypeEnum,
                                               MessageContentTypeEnum messageContentTypeEnum, MessageStatusEnum messageStatus,
                                               TerminalTypeEnum terminalType, Long messageId, Long sendId, String sendNickName,
                                               String messageContent, Date sendTime, Long friendId) {
        return ChatMessageDTO
                .builder()
                .messageConversationTypeEnum(messageConversationTypeEnum)
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

    private RabbitMQMessageBodyDTO builderMQMessageDTO(MessageConversationTypeEnum messageConversationTypeEnum,
                                                       MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId,
                                                       String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds,
                                                       MessageStatusEnum messageStatus, TerminalTypeEnum terminalType, Date sendTime) {
        return RabbitMQMessageBodyDTO.builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .data(ChatMessageDTO.builder().messageConversationTypeEnum(messageConversationTypeEnum)
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
        privateMessage.setVisibleToOneself(true);
        privateMessage.setCreateTime(new Date());
        privateMessage.setUpdateTime(new Date());
        privateMessageRepository.save(privateMessage);

        // 推送im消息
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE,
                DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageConversationTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L,
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
                builderMQMessageDTO(MessageConversationTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L,
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
                    builderMQMessageDTO(MessageConversationTypeEnum.PRIVATE_MESSAGE,
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
        String key = RedisKeyConstant.CURRENT_CONVERSATION_NEXT_CONV_SEQ_KEY(uidA.toString(), uidB.toString());
        return redisUtil.increment(key, 1L);
    }
}
