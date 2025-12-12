package com.developer.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.*;
import com.developer.message.pojo.*;
import com.developer.message.repository.GroupMessageDeleteRepository;
import com.developer.message.repository.GroupMessageReadRepository;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.ConversationService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupMemberResponseRpcDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl extends AbstractMessageAdapterService {

    private final RedisUtil redisUtil;
    private final RabbitMQUtil rabbitMQUtil;
    private final MessageLikeService messageLikeService;
    private final GroupMessageRepository groupMessageRepository;
    private final RpcClient rpcClient;
    private final GroupMessageReadRepository groupMessageReadRepository;
    private final ConversationService conversationService;
    private final GroupMessageDeleteRepository groupMessageDeleteRepository;

    /**
     * 消息主体类型
     *
     * @return
     */
    @Override
    public MessageConversationTypeEnum messageMainType() {
        return MessageConversationTypeEnum.GROUP_MESSAGE;
    }

    /*
    拉取消息
     */
    @Override
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req) {
        List<LoadMessageListResponseDTO> list = new ArrayList<>();

        // 首先校验当前群是否存在
        DeveloperResult<List<GroupInfoResponseRpcDTO>> execute = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!execute.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), execute.getMsg());
        }

        GroupInfoResponseRpcDTO groupInfo = execute.getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isNull(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "群不存在,拉取消息失败");
        }

        // 判断当前聊天会话是否有新的消息
        // 和当前对象会话的maxSeq
        String maxSeqKey = RedisKeyConstant.CURRENT_CONVERSATION_MAX_SEQ_KEY(SelfUserInfoContext.selfUserInfo().getUserId().toString(), req.getTargetId().toString());
        Long maxSeq = Optional.ofNullable(redisUtil.get(maxSeqKey, Long.class)).orElse(0L);
        // 当前设备终端最大的convSeq
        String lastSeqKey = RedisKeyConstant.CURRENT_TERMINAL_LAST_SEQ_KEY(SelfUserInfoContext.selfUserInfo().getUserId().toString(), req.getTargetId().toString(), req.getTerminalType().code());
        Long lastSeq = Optional.ofNullable(redisUtil.get(lastSeqKey, Long.class)).orElse(0L);
        if (maxSeq > 0 && lastSeq > 0) {
            if (maxSeq.equals(lastSeq)) {
                return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
            }
        }

        // 获取用户当前群的最新聊天记录
        List<GroupMessagePO> messageList = groupMessageRepository.findMessageList(req.getTargetId(), req.getTargetId());

        // 获取在此群中未读的消息
        List<GroupMessageReadPO> unreadMsgList = new ArrayList<>();
        List<GroupMessageReadPO> readMessageList = groupMessageReadRepository.findUserReadGroupMessageList(groupInfo.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if (CollUtil.isNotEmpty(readMessageList)) {
            Set<Long> readMsgIdSet = readMessageList.stream()
                    .map(GroupMessageReadPO::getMsgId)
                    .collect(Collectors.toSet());

            unreadMsgList = messageList.stream()
                    .map(GroupMessagePO::getId)
                    .filter(id -> !readMsgIdSet.contains(id))
                    .map(id -> {
                        GroupMessageReadPO po = new GroupMessageReadPO();
                        po.setGroupId(groupInfo.getGroupId());
                        po.setUserId(SelfUserInfoContext.selfUserInfo().getUserId());
                        po.setMsgId(id);
                        po.setReadTime(new Date());
                        return po;
                    })
                    .collect(Collectors.toList());
        }

        // 未读改为已读
        if (CollUtil.isNotEmpty(unreadMsgList)) {
            // 新增已读回执
            groupMessageReadRepository.saveBatch(unreadMsgList);

            // 修改消息表的已读数
            groupMessageRepository.updateMessageReadCount(groupInfo.getGroupId(), unreadMsgList.stream().map(GroupMessageReadPO::getMsgId).collect(Collectors.toList()));
        }

        // 修改当前终端的lastSeq
        redisUtil.set(lastSeqKey, Collections.max(messageList.stream().map(GroupMessagePO::getId).collect(Collectors.toList())));

        // 聚合参数返回
        list = messageList.stream().map(x -> {
            LoadMessageListResponseDTO dto = new LoadMessageListResponseDTO();
            dto.setId(x.getId());
            dto.setSendId(x.getSendId());
            dto.setMessageContent(x.getMessageContent());
            dto.setMessageContentType(MessageContentTypeEnum.fromCode(x.getMessageContentType()));
            dto.setMessageStatus(MessageStatusEnum.fromCode(x.getMessageStatus()));
            dto.setSendTime(x.getSendTime());
            dto.setConvSeq(x.getMsgSeq());
            dto.setSendNickName(x.getSendNickName());
            dto.setReferenceId(x.getReferenceId());
            dto.setLikeCount(x.getLikeCount());

            dto.setGroupId(x.getGroupId());
            dto.setAtUserIds(x.getAtUserIds());
            dto.setUnReadCount(0L);
            dto.setReadStatus(0);
            return dto;
        }).collect(Collectors.toList());
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    /*
    发送消息
     */
    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        // 1、校验群信息
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!groupResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupResult.getMsg());
        }
        // 1.1、是否在群内
        GroupInfoResponseRpcDTO groupInfo = groupResult.getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "群不存在,发送失败");
        }

        assert groupInfo != null;
        if (!groupInfo.getQuit()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你已退出群聊,发送失败");
        }

        // 消息入库
        GroupMessagePO message = new GroupMessagePO();
        message.setGroupId(groupInfo.getGroupId());
        message.setMsgSeq(getCurrentConversationNextConvSeq(groupInfo.getGroupId()));
        message.setSendId(SelfUserInfoContext.selfUserInfo().getUserId());
        message.setSendNickName(SelfUserInfoContext.selfUserInfo().getNickName());
        message.setSenderRole(groupInfo.getGroupRole().code());
        message.setMessageContentType(req.getMessageContentType().code());
        message.setMessageContent(req.getMessageContent());
        message.setReferenceId(req.getReferenceId());
        message.setAtUserIds(req.getAtUserIds().toString());
        message.setMessageStatus(MessageStatusEnum.UNSEND.code());
        message.setDeleted(false);
        message.setLikeCount(0L);
        message.setReadCount(0L);
        message.setSendTime(new Date());
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        this.groupMessageRepository.save(message);

        // 需要接受消息的成员
        DeveloperResult<List<GroupMemberResponseRpcDTO>> groupMemberResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.findGroupMemberList(groupInfo.getGroupId()));
        if (!groupMemberResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupMemberResult.getMsg());
        }
        List<Long> groupMemberUserIds = groupMemberResult.getData().stream().filter(x -> !x.getMemberUserId().equals(SelfUserInfoContext.selfUserInfo().getUserId())).map(GroupMemberResponseRpcDTO::getMemberUserId).collect(Collectors.toList());
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(req.getMessageMainType(), req.getMessageContentType(), message.getId(), message.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), req.getMessageContent(), groupMemberUserIds, req.getAtUserIds(), MessageStatusEnum.fromCode(message.getMessageStatus()), TerminalTypeEnum.WEB, message.getSendTime()));

        // 红包转账消息调用支付接口
        DeveloperResult<Boolean> invokedPayResult = invokePay(message.getId(), rpcClient, req);
        if (!invokedPayResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), invokedPayResult.getMsg());
        }

        GroupMessageDTO data = new GroupMessageDTO();
        data.setId(message.getId());
        data.setReadCount(0L);
        data.setUnReadCount((long) groupMemberUserIds.size());

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), data);
    }

    /*
    已读消息
     */
    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        // 1、校验当前所有聊天会话是否有未读消息
        List<ChatConversationListResponseDTO> conversationList = conversationService.findChatConversationList().getData();
        if (CollUtil.isEmpty(conversationList)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不存在会话内容");
        }
        boolean existUnread = conversationList.stream().anyMatch(x -> x.getTargetId().equals(req.getTargetId()) && x.getUnreadCount() > 0);
        if (!existUnread) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "当前会话不存在未读消息");
        }

        // 2、校验是否在此群中
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupInfoResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!groupInfoResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupInfoResult.getMsg());
        }
        GroupInfoResponseRpcDTO groupInfo = groupInfoResult.getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你不在此群中,操作失败");
        }

        // 3、在此群中是否存在未读消息,查询已读回执
        List<GroupMessageReadPO> readMessageList = groupMessageReadRepository.findUserReadGroupMessageList(req.getTargetId(), SelfUserInfoContext.selfUserInfo().getUserId());
        List<Long> readMessageIds = readMessageList.stream().map(GroupMessageReadPO::getMsgId).collect(Collectors.toList());
        // 当前用户在此群中所有未读的消息
        List<GroupMessagePO> unreadMessageList = groupMessageRepository.findUnreadMessageList(req.getTargetId(), readMessageIds);

        // 4、新增已读回执
        List<GroupMessageReadPO> readPOS = unreadMessageList.stream().map(x -> {
            GroupMessageReadPO dto = new GroupMessageReadPO();
            dto.setMsgId(x.getId());
            dto.setGroupId(x.getGroupId());
            dto.setUserId(SelfUserInfoContext.selfUserInfo().getUserId());
            dto.setReadTime(new Date());
            return dto;
        }).collect(Collectors.toList());
        groupMessageReadRepository.saveBatch(readPOS);

        // 5、修改消息已读数
        groupMessageRepository.updateMessageReadCount(req.getTargetId(), unreadMessageList.stream().map(GroupMessagePO::getId).collect(Collectors.toList()));

        // 6、修改会话列表
        UpsertConversationRequestDTO conversationRequestDTO = new UpsertConversationRequestDTO();
        conversationRequestDTO.setTargetId(req.getTargetId());
        conversationRequestDTO.setUnreadCount(0);
        DeveloperResult<Boolean> updateConversationResult = conversationService.upsertCurrentConversation(conversationRequestDTO);
        if (!updateConversationResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), updateConversationResult.getMsg());
        }

        // 7、向消息发送者发送已读通知
        for (GroupMessagePO item : unreadMessageList) {
            rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageConversationTypeEnum.GROUP_MESSAGE, MessageContentTypeEnum.TEXT, item.getId(), req.getTargetId(), SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), "", Collections.singletonList(item.getSendId()), new ArrayList<>(), MessageStatusEnum.READED, TerminalTypeEnum.WEB, new Date()));
        }

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    撤回消息
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(WithdrawMessageRequestDTO req) {
        // 1、校验是否在此群中
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupInfoResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!groupInfoResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupInfoResult.getMsg());
        }
        GroupInfoResponseRpcDTO groupInfo = groupInfoResult.getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isNull(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你不在此群中,操作失败");
        }

        // 2、消息校验
        GroupMessagePO groupMessage = groupMessageRepository.findMessageById(req.getTargetId(), req.getMessageId());
        if (groupMessage == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "消息不存在");
        }
        if (!groupMessage.getSendId().equals(SelfUserInfoContext.selfUserInfo().getUserId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "无法撤回不是自己发送的消息");
        }

        // 3、修改消息状态
        groupMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        groupMessageRepository.updateById(groupMessage);

        // 4、通知群成员消息撤回
        DeveloperResult<List<GroupMemberResponseRpcDTO>> groupMemberExecute = RpcExecutor.execute(() -> rpcClient.groupRpcService.findGroupMemberList(groupInfo.getGroupId()));
        if(!groupMemberExecute.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),groupMemberExecute.getMsg());
        }
        List<Long> memberUserIds = groupMemberExecute.getData().stream().filter(x->!SelfUserInfoContext.selfUserInfo().getUserId().equals(x.getMemberUserId())).map(GroupMemberResponseRpcDTO::getMemberUserId).collect(Collectors.toList());
        String message = String.format("%s 撤回了一条消息", groupInfo.getMemberAlias());
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageConversationTypeEnum.GROUP_MESSAGE, MessageContentTypeEnum.TEXT, groupMessage.getId(), groupMessage.getGroupId(), groupMessage.getSendId(), groupMessage.getSendNickName(), message, memberUserIds, new ArrayList<>(), MessageStatusEnum.fromCode(groupMessage.getMessageStatus()), TerminalTypeEnum.WEB, new Date()));

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    查询历史记录
     */
    @Override
    public DeveloperResult<List<QueryHistoryMessageResponseDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage() > 0 ? req.getPage() : 1);
        req.setSize(req.getSize() > 0 ? req.getSize() : 10);
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    删除消息
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        // 1、校验是否在此群中
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupInfoResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!groupInfoResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupInfoResult.getMsg());
        }
        GroupInfoResponseRpcDTO groupInfo = groupInfoResult.getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isNull(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你不在此群中,操作失败");
        }

        // 2、消息校验
        GroupMessagePO groupMessage = groupMessageRepository.findMessageById(req.getTargetId(), req.getMessageId());
        if (groupMessage == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "消息不存在");
        }

        // 3、判断消息是否已删除
        List<GroupMessageDeletePO> deleteMessages = groupMessageDeleteRepository.findMessages(groupInfo.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if(CollUtil.isNotEmpty(deleteMessages))
        {
            boolean b = deleteMessages.stream().anyMatch(x -> x.getMsgId().equals(req.getMessageId()));
            if(b){
                return DeveloperResult.error(SerialNoHolder.getSerialNo(),"消息已不存在");
            }
        }

        // 4、删除入库
        GroupMessageDeletePO groupMessageDeletePO = new GroupMessageDeletePO();
        groupMessageDeletePO.setMsgId(groupMessage.getId());
        groupMessageDeletePO.setMsgSeq(groupMessage.getMsgSeq());
        groupMessageDeletePO.setGroupId(groupMessage.getGroupId());
        groupMessageDeletePO.setDeleteTime(new Date());
        groupMessageDeletePO.setUserId(SelfUserInfoContext.selfUserInfo().getUserId());
        groupMessageDeleteRepository.save(groupMessageDeletePO);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    回复
     */
    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        // 1、校验是否在此群中
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupInfoResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        if (!groupInfoResult.getIsSuccessful()) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), groupInfoResult.getMsg());
        }
        GroupInfoResponseRpcDTO groupInfo = groupInfoResult.getData().stream().filter(x -> x.getGroupId().equals(req.getGroupId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(groupInfo)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "你不在此群中,操作失败");
        }

        // 2、消息校验
        GroupMessagePO groupMessage = groupMessageRepository.findMessageById(req.getGroupId(), id);
        if (groupMessage == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "消息不存在");
        }

        sendMessage(SendMessageRequestDTO.builder().targetId(req.getReceiverId()).messageContent(req.getMessageContent())
                .messageMainType(req.getMessageMainType()).messageContentType(req.getMessageContentType()).atUserIds(req.getAtUserIds())
                .referenceId(id).build());
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    收藏
     */
    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    /*
    转发
     */
    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        GroupMessagePO groupMessagePO = groupMessageRepository.getById(req.getMessageId());
        if (groupMessagePO == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "转发消息本体不存在");
        }

        for (Long userId : req.getUserIdList()) {
            SendMessageRequestDTO dto = new SendMessageRequestDTO();
            dto.setMessageContent(groupMessagePO.getMessageContent());
            dto.setTargetId(userId);
            dto.setMessageContentType(MessageContentTypeEnum.fromCode(groupMessagePO.getMessageContentType()));
            dto.setMessageMainType(MessageConversationTypeEnum.GROUP_MESSAGE);

            this.sendMessage(dto);
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageConversationTypeEnum.GROUP_MESSAGE);
    }

    /*
    取消点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageConversationTypeEnum.GROUP_MESSAGE);
    }

    private long getCurrentConversationNextConvSeq(Long groupId) {
        String key = RedisKeyConstant.CURRENT_GROUP_CONVERSATION_NEXT_CONV_SEQ_KEY(groupId.toString());
        return redisUtil.increment(key, 1L);
    }

    private ChatMessageDTO builderMQMessageDTO(MessageConversationTypeEnum messageConversationTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, MessageStatusEnum messageStatus, TerminalTypeEnum terminalType, Date sendTime) {
        return ChatMessageDTO
                .builder()
                .messageConversationTypeEnum(messageConversationTypeEnum)
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
                .sendTime(sendTime)
                .build();
    }
}
