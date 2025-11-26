package com.developer.message.service.impl;

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
import com.developer.message.repository.GroupMessageMemberReceiveRecordRepository;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.util.RabbitMQUtil;
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
    private final PaymentClient paymentClient;
    private final GroupInfoClient groupInfoClient;
    private final GroupMemberClient groupMemberClient;
    private final MessageLikeService messageLikeService;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupMessageMemberReceiveRecordRepository groupMessageMemberReceiveRecordRepository;

    /**
     * 消息主体类型
     * @return
     */
    @Override
    public MessageMainTypeEnum messageMainType() {
        return MessageMainTypeEnum.GROUP_MESSAGE;
    }

    /**
     * 拉取消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = groupInfoClient.getSelfJoinAllGroupInfo(serialNo).getData();
        if (joinGroupInfoList.isEmpty()) {
            return DeveloperResult.success(serialNo);
        }

        List<Long> groupIds = joinGroupInfoList.stream().map(SelfJoinGroupInfoDTO::getGroupId).collect(Collectors.toList());

        // 当前用户有多少群消息未读
        List<GroupMessageMemberReceiveRecordPO> unreadMessageList = groupMessageMemberReceiveRecordRepository.findAllUnreadMessageList(userId);
        // 当前用户发送的群消息有多少已读未读
        List<GroupMessageMemberReceiveRecordPO> curUserSendMessageList = groupMessageMemberReceiveRecordRepository.findAllMessageBySendId(userId);

        Date minDate = DateTimeUtils.addMonths(new Date(), -3);
        List<GroupMessagePO> messages = groupMessageRepository.find(req.getLastSeq(), minDate, groupIds);
        List<SendMessageResultDTO> vos = messages.stream().map(x -> {
            GroupMessageDTO vo = BeanUtils.copyProperties(x, GroupMessageDTO.class);
            if (vo == null) {
                return null;
            }
            MessageStatusEnum messageStatus = unreadMessageList.stream().anyMatch(z ->
                    Objects.equals(z.getGroupId(), x.getGroupId()) && Objects.equals(z.getMessageId(), x.getId())
            ) ? MessageStatusEnum.UNSEND : MessageStatusEnum.READED;

            vo.setMessageStatus(messageStatus);

            if (vo.getSendId().equals(userId)) {
                Map<Long, Long> messageCounts = curUserSendMessageList.stream().collect(Collectors.groupingBy(GroupMessageMemberReceiveRecordPO::getMessageId, Collectors.summingLong(m -> m.getStatus() == 0 ? 1 : m.getStatus() == 3 ? 1 : 0)));
                long unReadCount = messageCounts.getOrDefault(vo.getId(), 0L);
                long readCount = messageCounts.getOrDefault(vo.getId(), 0L);
                vo.setUnReadCount(unReadCount);
                vo.setReadCount(readCount);
            }
            return vo;
        }).collect(Collectors.toList());

        return DeveloperResult.success(serialNo, null);
    }

    /**
     * 发送消息
     *
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        String serialNo = SerialNoHolder.getSerialNo();
        DeveloperResult<GroupInfoDTO> findGroupResult = groupInfoClient.findGroup(FindGroupRequestDTO.builder().groupId(req.getGroupId()).serialNo(serialNo).build());
        if (!findGroupResult.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, findGroupResult.getMsg());
        }

        GroupInfoDTO groupInfoDTO = findGroupResult.getData();
        if (groupInfoDTO.getDeleted()) {
            return DeveloperResult.error(serialNo, "群已解散");
        }

        List<SelfJoinGroupInfoDTO> joinGroupInfoList = groupInfoClient.getSelfJoinAllGroupInfo(serialNo).getData();
        if (joinGroupInfoList.stream().noneMatch(x -> x.getGroupId().equals(req.getGroupId()) && x.getQuit())) {
            return DeveloperResult.error(serialNo, "您已不在该群聊中,无法发送消息");
        }

        // 消息入库
        GroupMessagePO message = this.createGroupMessageMode(req.getGroupId(), userId, nickName, req.getAtUserIds(), req.getMessageContent(), req.getMessageContentType());
        this.groupMessageRepository.save(message);

        // 需要接受消息的成员
        List<Long> receiverIds = groupMemberClient.findGroupMemberUserId(FindGroupMemberUserIdRequestDTO.builder().groupId(groupInfoDTO.getId()).serialNo(serialNo).build()).getData();
        receiverIds = receiverIds.stream().filter(id -> !userId.equals(id)).collect(Collectors.toList());

        List<GroupMessageMemberReceiveRecordPO> receiveRecords = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            GroupMessageMemberReceiveRecordPO record = new GroupMessageMemberReceiveRecordPO();
            record.setGroupId(req.getGroupId());
            record.setReceiverId(receiverId);
            record.setStatus(0);
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            record.setSendId(userId);
            record.setMessageId(message.getId());
            receiveRecords.add(record);
        }

        groupMessageMemberReceiveRecordRepository.saveBatch(receiveRecords);

        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(req.getMessageMainType(), req.getMessageContentType(), message.getId(), message.getGroupId(), userId, nickName, req.getMessageContent(), receiverIds, req.getAtUserIds(), MessageStatusEnum.fromCode(message.getMessageStatus()), TerminalTypeEnum.WEB, message.getSendTime()));


        GroupMessageDTO data = new GroupMessageDTO();
        data.setId(message.getId());
        data.setReadCount(0L);
        data.setUnReadCount((long) receiverIds.size());

        // 同步修改红包消息状态
        if(req.getMessageContentType()== MessageContentTypeEnum.RED_PACKETS || req.getMessageContentType() == MessageContentTypeEnum.TRANSFER){
            paymentClient.modifyRedPacketsMessageStatus(ModifyRedPacketsMessageStatusRequestDTO.builder().serialNo(serialNo).messageStatus(1).build());
        }

        return DeveloperResult.success(serialNo, data);
    }

    /**
     * 已读消息
     *
     * @param req
     * @return
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

    /**
     * 撤回消息
     *
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(RecallMessageRequestDTO req) {
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

    /**
     * 查询历史记录
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage() > 0 ? req.getPage() : 1);
        req.setSize(req.getSize() > 0 ? req.getSize() : 10);
        String serialNo = SerialNoHolder.getSerialNo();
        long stIdx = (req.getPage() - 1) * req.getSize();

        SelfJoinGroupInfoDTO selfJoinGroupInfoDTO = groupInfoClient.getSelfJoinAllGroupInfo(serialNo).getData().stream().filter(x -> x.getGroupId().equals(req.getTargetId()) && !x.getQuit()).findFirst().get();
        if (selfJoinGroupInfoDTO == null) {
            return DeveloperResult.error(serialNo, "您已不在群聊");
        }

        List<GroupMessagePO> messages = groupMessageRepository.findHistoryMessage(req.getTargetId(), selfJoinGroupInfoDTO.getCreatedTime(), stIdx, req.getSize());
        List<SendMessageResultDTO> list = messages.stream().map(x -> BeanUtils.copyProperties(x, GroupMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo, list);
    }

    /**
     * 删除消息
     *
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        return DeveloperResult.success(serialNo);
    }

    /**
     * 回复
     * @param id
     * @param req
     * @return
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

    /**
     * 收藏
     *
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    /**
     * 转发
     * @param req
     * @return
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

    /**
     * 点赞
     * @param req
     * @return
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageMainTypeEnum.GROUP_MESSAGE);
    }

    /**
     * 取消点赞
     *
     * @param req
     * @return
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageMainTypeEnum.GROUP_MESSAGE);
    }

    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return null;
    }

    private GroupMessagePO createGroupMessageMode(Long groupId, Long sendId, String sendNickName, List<Long> atUserIds, String message, MessageContentTypeEnum messageContentType) {
        GroupMessagePO groupMessage = new GroupMessagePO();
        groupMessage.setGroupId(groupId);
        groupMessage.setSendId(sendId);
        groupMessage.setSendNickName(sendNickName);
        groupMessage.setMessageContent(message);
        groupMessage.setMessageContentType(messageContentType.code());
        groupMessage.setMessageStatus(0);
        groupMessage.setSendTime(new Date());
        if (atUserIds != null) {
            groupMessage.setAtUserIds(atUserIds.toString());
        }
        return groupMessage;
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
