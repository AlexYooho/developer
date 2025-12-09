package com.developer.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.enums.message.MessageStatusEnum;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.client.GroupInfoClient;
import com.developer.message.client.GroupMemberClient;
import com.developer.message.client.PaymentClient;
import com.developer.message.dto.*;
import com.developer.message.pojo.GroupMessageMemberReceiveRecordPO;
import com.developer.message.pojo.GroupMessagePO;
import com.developer.message.pojo.GroupMessageReadPO;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.GroupMessageMemberReceiveRecordRepository;
import com.developer.message.repository.GroupMessageReadRepository;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupMemberResponseRpcDTO;
import com.developer.rpc.dto.payment.request.InvokeRedPacketsTransferRequestRpcDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl extends AbstractMessageAdapterService {

    private final RedisUtil redisUtil;
    private final RabbitMQUtil rabbitMQUtil;
    private final GroupInfoClient groupInfoClient;
    private final GroupMemberClient groupMemberClient;
    private final MessageLikeService messageLikeService;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupMessageMemberReceiveRecordRepository groupMessageMemberReceiveRecordRepository;
    private final RpcClient rpcClient;
    private final GroupMessageReadRepository groupMessageReadRepository;

    /**
     * 消息主体类型
     *
     * @return
     */
    @Override
    public MessageMainTypeEnum messageMainType() {
        return MessageMainTypeEnum.GROUP_MESSAGE;
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
        if (ObjectUtil.isEmpty(groupInfo)) {
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
        GroupInfoResponseRpcDTO groupInfo = groupResult.getData().stream().filter(x -> x.getGroupId().equals(req.getGroupId())).findFirst().orElse(null);
        if(ObjectUtil.isEmpty(groupInfo)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"群不存在,发送失败");
        }

        assert groupInfo != null;
        if(!groupInfo.getQuit()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"你已退出群聊,发送失败");
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
        if(!groupMemberResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),groupMemberResult.getMsg());
        }
        List<Long> groupMemberUserIds = groupMemberResult.getData().stream().filter(x -> !x.getMemberUserId().equals(SelfUserInfoContext.selfUserInfo().getUserId())).map(GroupMemberResponseRpcDTO::getMemberUserId).collect(Collectors.toList());
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(req.getMessageMainType(), req.getMessageContentType(), message.getId(), message.getGroupId(), SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), req.getMessageContent(), groupMemberUserIds, req.getAtUserIds(), MessageStatusEnum.fromCode(message.getMessageStatus()), TerminalTypeEnum.WEB, message.getSendTime()));

        // 红包转账消息调用支付接口
        DeveloperResult<Boolean> invokedPayResult = invokePay(message.getId(), rpcClient, req);
        if(!invokedPayResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),invokedPayResult.getMsg());
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
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupMessagePO lastMessage = groupMessageRepository.findLastMessage(req.getTargetId());
        if (Objects.isNull(lastMessage)) {
            return DeveloperResult.success(serialNo);
        }

        // 修改群已读状态
        List<GroupMessageMemberReceiveRecordPO> records = groupMessageMemberReceiveRecordRepository.findCurGroupUnreadRecordList(req.getTargetId(), userId);
        records.forEach(x -> {
            // 通知前端
            rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.GROUP_MESSAGE, MessageContentTypeEnum.TEXT, x.getMessageId(), req.getTargetId(), userId, nickName, "", Collections.singletonList(x.getSendId()), new ArrayList<>(), MessageStatusEnum.READED, TerminalTypeEnum.WEB, new Date()));
            x.setStatus(MessageStatusEnum.READED.code());
            groupMessageMemberReceiveRecordRepository.updateById(x);
        });

        String key = StrUtil.join(",", RedisKeyConstant.IM_GROUP_READED_POSITION, req.getTargetId(), userId);
        redisUtil.set(key, lastMessage.getId(), 3600 * 24L, TimeUnit.SECONDS);
        return DeveloperResult.success(serialNo);
    }

    /*
    撤回消息
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(WithdrawMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        GroupMessagePO groupMessage = groupMessageRepository.getById(req.getMessageId());
        if (groupMessage == null) {
            return DeveloperResult.error(serialNo, "消息不存在");
        }

        if (!groupMessage.getSendId().equals(userId)) {
            return DeveloperResult.error(serialNo, "无法撤回不是自己发送的消息");
        }

        DeveloperResult<List<SelfJoinGroupInfoDTO>> developerResult = groupInfoClient.getSelfJoinAllGroupInfo(serialNo);
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = developerResult.getData();
        SelfJoinGroupInfoDTO selfJoinGroupInfoDTO = joinGroupInfoList.stream().filter(x -> x.getGroupId().equals(groupMessage.getGroupId()) && x.getQuit()).findFirst().get();
        if (selfJoinGroupInfoDTO == null) {
            return DeveloperResult.error(serialNo, "您已不在该群聊中,无法撤回消息");
        }

        groupMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        groupMessageRepository.updateById(groupMessage);

        List<Long> receiverIds = groupMemberClient.findGroupMemberUserId(FindGroupMemberUserIdRequestDTO.builder().groupId(groupMessage.getId()).serialNo(serialNo).build()).getData();
        receiverIds = receiverIds.stream().filter(x -> !userId.equals(x)).collect(Collectors.toList());

        String message = String.format("%s 撤回了一条消息", selfJoinGroupInfoDTO.getAliasName());

        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.GROUP_MESSAGE, MessageContentTypeEnum.TEXT, groupMessage.getId(), groupMessage.getGroupId(), groupMessage.getSendId(), groupMessage.getSendNickName(), message, receiverIds, new ArrayList<>(), MessageStatusEnum.fromCode(groupMessage.getMessageStatus()), TerminalTypeEnum.WEB, new Date()));

        return DeveloperResult.success(serialNo);
    }

    /*
    查询历史记录
     */
    @Override
    public DeveloperResult<List<QueryHistoryMessageResponseDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage() > 0 ? req.getPage() : 1);
        req.setSize(req.getSize() > 0 ? req.getSize() : 10);
        String serialNo = SerialNoHolder.getSerialNo();
        long stIdx = (req.getPage() - 1) * req.getSize();

        SelfJoinGroupInfoDTO selfJoinGroupInfoDTO = groupInfoClient.getSelfJoinAllGroupInfo(serialNo).getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId()) && !x.getQuit()).findFirst().get();
        if (selfJoinGroupInfoDTO == null) {
            return DeveloperResult.error(serialNo, "您已不在群聊");
        }

        List<GroupMessagePO> messages = groupMessageRepository.findHistoryMessage(req.getTargetId(), selfJoinGroupInfoDTO.getCreatedTime(), stIdx, req.getSize());
        List<QueryHistoryMessageResponseDTO> list = messages.stream().map(x -> BeanUtils.copyProperties(x, QueryHistoryMessageResponseDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo, list);
    }

    /*
    删除消息
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        return DeveloperResult.success(serialNo);
    }

    /*
    回复
     */
    @Override
    public DeveloperResult<Boolean> replyMessage(Long id, ReplyMessageRequestDTO req) {
        GroupMessagePO groupMessagePO = groupMessageRepository.getById(id);
        String serialNo = SerialNoHolder.getSerialNo();
        if (groupMessagePO == null) {
            return DeveloperResult.error(serialNo, "回复消息不存在");
        }
        this.sendMessage(SendMessageRequestDTO.builder().serialNo(serialNo).receiverId(req.getReceiverId()).messageContent(req.getMessageContent())
                .messageMainType(req.getMessageMainType()).messageContentType(req.getMessageContentType()).groupId(req.getGroupId()).atUserIds(req.getAtUserIds())
                .referenceId(id).build());
        return DeveloperResult.success(serialNo);
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
        String serialNo = SerialNoHolder.getSerialNo();
        req.setSerialNo(serialNo);
        if (groupMessagePO == null) {
            return DeveloperResult.error(serialNo, "转发消息本体不存在");
        }

        for (Long userId : req.getUserIdList()) {
            SendMessageRequestDTO dto = new SendMessageRequestDTO();
            dto.setSerialNo(serialNo);
            dto.setMessageContent(groupMessagePO.getMessageContent());
            dto.setReceiverId(userId);
            dto.setMessageContentType(MessageContentTypeEnum.fromCode(groupMessagePO.getMessageContentType()));
            dto.setMessageMainType(MessageMainTypeEnum.GROUP_MESSAGE);

            this.sendMessage(dto);
        }
        return DeveloperResult.success(serialNo);
    }

    /*
    点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageMainTypeEnum.GROUP_MESSAGE);
    }

    /*
    取消点赞
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageMainTypeEnum.GROUP_MESSAGE);
    }

    private long getCurrentConversationNextConvSeq(Long groupId) {
        String key = RedisKeyConstant.CURRENT_GROUP_CONVERSATION_NEXT_CONV_SEQ_KEY(groupId.toString());
        return redisUtil.increment(key, 1L);
    }

    private ChatMessageDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, MessageStatusEnum messageStatus, TerminalTypeEnum terminalType, Date sendTime) {
        return ChatMessageDTO
                .builder()
                .messageMainTypeEnum(messageMainTypeEnum)
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
