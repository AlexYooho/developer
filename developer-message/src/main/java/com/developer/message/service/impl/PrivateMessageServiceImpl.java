package com.developer.message.service.impl;

import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.*;
import com.developer.framework.enums.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.client.FriendClient;
import com.developer.message.client.PaymentClient;
import com.developer.message.dto.*;
import com.developer.message.param.IsFriendParam;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.AbstractMessageAdapterService;
import com.developer.message.service.FriendService;
import com.developer.message.service.MessageLikeService;
import com.developer.message.service.MessageService;
import com.developer.message.util.RabbitMQUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 消息主体类型
     * @return
     */
    @Override
    public MessageMainTypeEnum messageMainType() {
        return MessageMainTypeEnum.PRIVATE_MESSAGE;
    }

    /**
     * 拉取最新消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<SendMessageResultDTO>> loadMessage(LoadMessageRequestDTO req) {
        List<SendMessageResultDTO> list = new ArrayList<>();
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        // 优化：按终端类型区分游标
        MessageTerminalTypeEnum terminalType = req.getTerminalType();
        String key = RedisKeyConstant.DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(userId, terminalType);
        Long maxMessageId = redisUtil.get(key, Long.class) == null ? 0L : redisUtil.get(key, Long.class);
        if (maxMessageId != 0L && req.getMinId().equals(maxMessageId)) {
            return DeveloperResult.success(serialNo, list);
        }

        List<PrivateMessagePO> messages = privateMessageRepository.getMessageListByUserId(req.getMinId(), userId);
        List<Long> ids = messages.stream()
                .filter(x -> !x.getSendId().equals(userId) && x.getMessageStatus().equals(MessageStatusEnum.UNSEND))
                .map(PrivateMessagePO::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.privateMessageRepository.updateMessageStatus(ids, MessageStatusEnum.SENDED);
        }

        // 只在有新消息时更新 maxMessageId，且取本次拉取到的消息的最大 id
        if (!messages.isEmpty()) {
            Long newMaxId = messages.stream().mapToLong(PrivateMessagePO::getId).max().getAsLong();
            redisUtil.set(key, newMaxId, 24, TimeUnit.HOURS);
        }
        // 如果没有新消息，不回写 redis，保持原有 maxMessageId
        list = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo, list);
    }

    /**
     * 发送消息
     * @param req
     * @return
     */
    @Transactional
    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();

        DeveloperResult<Boolean> friend = friendService.isFriend(userId, req.getReceiverId());
        if(!friend.getIsSuccessful()){
            return DeveloperResult.error(serialNo,friend.getMsg());
        }

        // 消息入库
        PrivateMessagePO privateMessage = this.saveMessage(userId,req);

        // 记录消息id，兼容多端游标
        MessageTerminalTypeEnum terminalType = req.getTerminalType() != null ? req.getTerminalType() : MessageTerminalTypeEnum.WEB;
        redisUtil.set(RedisKeyConstant.DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(userId, terminalType), privateMessage.getId(), 24, TimeUnit.HOURS);

        // 发送消息
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(req.getMessageMainType(),req.getMessageContentType(), privateMessage.getMessageStatus(), terminalType, privateMessage.getId(), userId, nickName, req.getMessageContent(),privateMessage.getSendTime(),req.getReceiverId()));

        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(privateMessage.getId());

        // 同步修改红包消息状态
        if(req.getMessageContentType()==MessageContentTypeEnum.RED_PACKETS || req.getMessageContentType() == MessageContentTypeEnum.TRANSFER){
            DeveloperResult<Boolean> modifyResult = paymentClient.modifyRedPacketsMessageStatus(ModifyRedPacketsMessageStatusRequestDTO.builder().serialNo(serialNo).messageStatus(1).build());
            if(!modifyResult.getIsSuccessful()){
                return DeveloperResult.error(serialNo, modifyResult.getMsg());
            }
        }

        return DeveloperResult.success(serialNo,dto);
    }

    /**
     * 已读消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, MessageStatusEnum.READED, MessageTerminalTypeEnum.WEB, 0L, userId, nickName, "",new Date(),req.getTargetId()));
        privateMessageRepository.updateMessageStatus(req.getTargetId(),userId,MessageStatusEnum.READED.code());
        return DeveloperResult.success(serialNo);
    }

    /**
     * 撤回消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> withdrawMessage(RecallMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        PrivateMessagePO privateMessage = privateMessageRepository.getById(req.getMessageId());
        if(privateMessage==null){
            return DeveloperResult.error(serialNo,"消息不存在");
        }

        if(!privateMessage.getSendId().equals(userId)){
            return DeveloperResult.error(serialNo,"该消息不是你发送的,无法撤回");
        }

        if(System.currentTimeMillis()-privateMessage.getSendTime().getTime()> DeveloperConstant.ALLOW_RECALL_SECOND*1000){
            return DeveloperResult.error(serialNo,"消息发送已超过一定时间,无法撤回");
        }

        // 修改消息状态
        privateMessage.setMessageStatus(MessageStatusEnum.RECALL);
        privateMessageRepository.updateById(privateMessage);

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM,
                builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, MessageStatusEnum.RECALL, MessageTerminalTypeEnum.WEB, privateMessage.getId(), userId, nickName,"对方撤回了一条消息",new Date(), privateMessage.getReceiverId()));

        return DeveloperResult.success(serialNo);
    }

    /**
     * 获取历史消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<List<SendMessageResultDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req) {
        req.setPage(req.getPage()>0?req.getPage():1);
        req.setSize(req.getSize()>0?req.getSize():10);
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        long pageIndex = (req.getPage()-1)*req.getSize();
        List<PrivateMessagePO> list = privateMessageRepository.getHistoryMessageList(userId, req.getTargetId(), pageIndex, req.getSize());
        List<SendMessageResultDTO> collect = list.stream().map(a -> BeanUtils.copyProperties(a, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,collect);
    }

    /**
     * 新增消息
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        String serialNo = SerialNoHolder.getSerialNo();
        PrivateMessagePO privateMessage = PrivateMessagePO.builder()
                .messageStatus(dto.getMessageStatus())
                .messageContent(dto.getMessageContent())
                .sendId(dto.getSendId())
                .receiverId(dto.getReceiverId())
                .messageContentType(dto.getMessageContentType())
                .sendTime(new Date()).build();
        boolean isSuccess = privateMessageRepository.save(privateMessage);
        return DeveloperResult.success(serialNo,isSuccess);
    }

    /**
     * 删除消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        boolean isSuccess = privateMessageRepository.deleteChatMessage(userId,req.getTargetId());
        return DeveloperResult.success(serialNo,isSuccess);
    }

    /**
     * 回复消息
     * @param id
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> replyMessage(Long id,ReplyMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(id);
        String serialNo = SerialNoHolder.getSerialNo();
        if(messagePO==null){
            return DeveloperResult.error(serialNo,"回复消息不存在");
        }

        req.setReferenceId(id);
        this.sendMessage(SendMessageRequestDTO.builder().serialNo(serialNo).receiverId(req.getReceiverId()).messageContent(req.getMessageContent())
                .messageMainType(req.getMessageMainType()).messageContentType(req.getMessageContentType()).groupId(req.getGroupId()).atUserIds(req.getAtUserIds())
                .referenceId(id).build());
        return DeveloperResult.success(serialNo);
    }

    /**
     * 收藏
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> collectionMessage(CollectionMessageRequestDTO req) {
        return null;
    }

    /**
     * 转发消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> forwardMessage(ForwardMessageRequestDTO req) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(req.getMessageId());
        String serialNo = SerialNoHolder.getSerialNo();
        if(messagePO==null){
            return DeveloperResult.error(serialNo,"转发消息本体不存在");
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

    /**
     * 点赞消息
     * @param req
     * @return
     */
    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.like(req, MessageMainTypeEnum.PRIVATE_MESSAGE);
    }

    @Async
    @Transactional
    @Override
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(MessageLikeRequestDTO req) {
        return messageLikeService.unLike(req, MessageMainTypeEnum.PRIVATE_MESSAGE);
    }

    /**
     * 是否支付类型消息
     * @param messageContentTypeEnum
     * @return
     */
    @Override
    public Boolean isPaymentMessageType(MessageContentTypeEnum messageContentTypeEnum) {
        return messageContentTypeEnum == MessageContentTypeEnum.RED_PACKETS || messageContentTypeEnum == MessageContentTypeEnum.TRANSFER;
    }

    private PrivateMessagePO createPrivateMessageMode(Long sendId, Long receiverId, String message, MessageContentTypeEnum messageContentType, MessageStatusEnum messageStatus, Long referenceId){
        return PrivateMessagePO.builder()
                .sendId(sendId)
                .receiverId(receiverId)
                .messageContent(message)
                .messageContentType(messageContentType)
                .messageStatus(messageStatus)
                .sendTime(new Date())
                .referenceId(referenceId)
                .build();
    }

    /**
     * 消息入库
     * @param userId
     * @param req
     * @return
     */
    private PrivateMessagePO saveMessage(Long userId,SendMessageRequestDTO req){
        PrivateMessagePO privateMessage = createPrivateMessageMode(userId, req.getReceiverId(), req.getMessageContent(), req.getMessageContentType(), MessageStatusEnum.SENDED,req.getReferenceId());
        this.privateMessageRepository.save(privateMessage);
        return privateMessage;
    }

    /**
     * 构建mq消息dto
     */
    private ChatMessageDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, MessageStatusEnum messageStatus, MessageTerminalTypeEnum terminalType, Long messageId, Long sendId, String sendNickName, String messageContent, Date sendTime,Long friendId){
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

    /*
    好友申请接受消息
     */
    @Override
    public DeveloperResult<Boolean> friendApplyAcceptMessage() {
        return super.friendApplyAcceptMessage();
    }
}
