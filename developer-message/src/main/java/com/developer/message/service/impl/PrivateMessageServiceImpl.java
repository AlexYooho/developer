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
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.message.client.FriendClient;
import com.developer.message.client.PaymentClient;
import com.developer.message.dto.*;
import com.developer.message.param.IsFriendParam;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.MessageLikeService;
import com.developer.message.service.MessageService;
import com.developer.message.util.RabbitMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrivateMessageServiceImpl implements MessageService {

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MessageLikeService messageLikeService;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        String key = RedisKeyConstant.DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(userId);
        Long maxMessageId = redisUtil.get(key, Long.class) == null ? 0L:redisUtil.get(key, Long.class);
        if(req.getMinId().equals(maxMessageId)){
            return DeveloperResult.success(serialNo,list);
        }

        List<PrivateMessagePO> messages = privateMessageRepository.getMessageListByUserId(req.getMinId(), userId);
        List<Long> ids = messages.stream().filter(x -> !x.getSendId().equals(userId) && x.getMessageStatus().equals(MessageStatusEnum.UNSEND.code()))
                .map(PrivateMessagePO::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.privateMessageRepository.updateMessageStatus(ids, MessageStatusEnum.SENDED);
        }

        redisUtil.set(key,ids.stream().max(Long::compareTo).orElse(req.getMinId()),24, TimeUnit.HOURS);

        list = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(serialNo,list);
    }

    /**
     * 发送消息
     * @param req
     * @return
     */
    @Transactional
    @Override
    public DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();

        DeveloperResult<FriendInfoDTO> friend = friendClient.isFriend(IsFriendParam.builder().serialNo(serialNo).friendId(req.getReceiverId()).userId(userId).build());
        if(!friend.getIsSuccessful()){
            return DeveloperResult.error(serialNo,friend.getMsg());
        }

        // 消息入库
        PrivateMessagePO privateMessage = this.saveMessage(userId,req);

        String key = RedisKeyConstant.DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(userId);
        redisUtil.set(key,privateMessage.getId(),24, TimeUnit.HOURS);

        // 发送消息
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(req.getMessageMainType(),req.getMessageContentType(), privateMessage.getId(), 0L, userId, SelfUserInfoContext.selfUserInfo().getNickName(), req.getMessageContent(), Collections.singletonList(req.getReceiverId()),new ArrayList<>(), MessageStatusEnum.fromCode(privateMessage.getMessageStatus()), MessageTerminalTypeEnum.WEB,privateMessage.getSendTime()));

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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, "",Collections.singletonList(req.getTargetId()),new ArrayList<>(), MessageStatusEnum.READED, MessageTerminalTypeEnum.WEB,new Date()));
        privateMessageRepository.updateMessageStatus(req.getTargetId(),userId,MessageStatusEnum.READED.code());
        return DeveloperResult.success(serialNo);
    }

    /**
     * 撤回消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult<Boolean> recallMessage(RecallMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
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
        privateMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        privateMessageRepository.updateById(privateMessage);

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.sendMessage(serialNo,DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, privateMessage.getId(), 0L, userId, nickName,"对方撤回了一条消息", Collections.singletonList(privateMessage.getReceiverId()),new ArrayList<>(), MessageStatusEnum.RECALL, MessageTerminalTypeEnum.WEB,new Date()));

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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
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
        String serialNo = snowflakeNoUtil.getSerialNo(dto.getSerialNo());
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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
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
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        if(messagePO==null){
            return DeveloperResult.error(serialNo,"转发消息本体不存在");
        }

        for (Long userId : req.getUserIdList()) {
            SendMessageRequestDTO dto = SendMessageRequestDTO.builder()
                    .serialNo(serialNo)
                    .messageContent(messagePO.getMessageContent())
                    .receiverId(userId)
                    .messageContentType(MessageContentTypeEnum.fromCode(messagePO.getMessageContentType()))
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
                .messageContentType(messageContentType.code())
                .messageStatus(messageStatus.code())
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
     * @param messageMainTypeEnum
     * @param messageContentTypeEnum
     * @param messageId
     * @param groupId
     * @param sendId
     * @param sendNickName
     * @param messageContent
     * @param receiverIds
     * @param atUserIds
     * @param messageStatus
     * @param terminalType
     * @param sendTime
     * @return
     */
    private MessageDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, MessageStatusEnum messageStatus, MessageTerminalTypeEnum terminalType, Date sendTime){
        return MessageDTO.builder().messageMainTypeEnum(messageMainTypeEnum)
                .messageContentTypeEnum(messageContentTypeEnum)
                .messageId(messageId)
                .groupId(groupId)
                .sendId(sendId)
                .sendNickName(sendNickName)
                .messageContent(messageContent)
                .receiverIds(receiverIds)
                .atUserIds(atUserIds)
                .messageStatus(messageStatus.code())
                .terminalType(terminalType)
                .sendTime(sendTime).build();
    }
}
