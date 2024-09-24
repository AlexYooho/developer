package com.developer.message.service.impl;

import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.message.client.FriendClient;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.PrivateMessageDTO;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.MessageService;
import com.developer.message.util.RabbitMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrivateMessageServiceImpl implements MessageService {

    @Autowired
    private FriendClient friendClient;

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 拉取最新消息
     * @param minId
     * @return
     */
    @Override
    public DeveloperResult loadMessage(Long minId) {
        List<PrivateMessageDTO> list = new ArrayList<>();
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        Long maxMessageId = fetchAndModifyMessageId(userId,minId);
        if(maxMessageId<=minId){
            log.info("拉取消息,用户id:{},数量:{}", userId, 0);
            return DeveloperResult.success(list);
        }

        List<PrivateMessagePO> messages = privateMessageRepository.getMessageListByUserId(minId, userId);
        List<Long> ids = messages.stream().filter(x -> !x.getSendId().equals(userId) && x.getMessageStatus().equals(MessageStatusEnum.UNSEND.code()))
                .map(PrivateMessagePO::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            this.privateMessageRepository.updateMessageStatus(ids, MessageStatusEnum.SENDED);
        }

        log.info("拉取消息,用户id:{},数量:{}", userId, messages.size());
        list = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageDTO.class)).collect(Collectors.toList());

        return DeveloperResult.success(list);
    }

    /**
     * 发送消息
     * @param req
     * @return
     */
    @Override
    public DeveloperResult sendMessage(SendMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        DeveloperResult<Boolean> friend = friendClient.isFriend(userId, req.getReceiverId());
        boolean isFriend = friend.getData();
        if(!isFriend){
            return DeveloperResult.error("对方不是你好友,无法发送消息");
        }

        // 消息入库
        PrivateMessagePO privateMessage = this.saveMessage(userId,req);

        fetchAndModifyMessageId(userId,privateMessage.getId());

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.pushMQMessage(req.getMessageMainType(),req.getMessageContentType(), privateMessage.getId(), 0L, userId, nickName, req.getMessageContent(), Arrays.asList(req.getReceiverId()),new ArrayList<>(), privateMessage.getMessageStatus(), IMTerminalTypeEnum.WEB,privateMessage.getSendTime());

        return DeveloperResult.success(privateMessage.getId());
    }

    /**
     * 已读消息
     * @param friendId
     * @return
     */
    @Override
    public DeveloperResult readMessage(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, "", Collections.singletonList(friendId),new ArrayList<>(), MessageStatusEnum.READED.code(), IMTerminalTypeEnum.WEB,new Date());
        privateMessageRepository.updateMessageStatus(friendId,userId,MessageStatusEnum.READED.code());
        return DeveloperResult.success();
    }

    /**
     * 撤回消息
     * @param id
     * @return
     */
    @Override
    public DeveloperResult<Boolean> recallMessage(Long id) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        PrivateMessagePO privateMessage = privateMessageRepository.getById(id);
        if(privateMessage==null){
            return DeveloperResult.error("消息不存在");
        }

        if(!privateMessage.getSendId().equals(userId)){
            return DeveloperResult.error("该消息不是你发送的,无法撤回");
        }

        if(System.currentTimeMillis()-privateMessage.getSendTime().getTime()> DeveloperConstant.ALLOW_RECALL_SECOND*1000){
            return DeveloperResult.error("消息发送已超过一定时间,无法撤回");
        }

        // 修改消息状态
        privateMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        privateMessageRepository.updateById(privateMessage);

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, privateMessage.getId(), 0L, userId, nickName, "对方撤回了一条消息",Arrays.asList(privateMessage.getReceiverId()),new ArrayList<>(), MessageStatusEnum.RECALL.code(), IMTerminalTypeEnum.WEB,new Date());

        return DeveloperResult.success();
    }

    /**
     * 获取历史消息
     * @param friendId
     * @param page
     * @param size
     * @return
     */
    @Override
    public DeveloperResult findHistoryMessage(Long friendId, Long page, Long size) {
        page = page>0?page:1;
        size = size>0?size:10;
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        long pageIndex = (page-1)*size;
        List<PrivateMessagePO> list = privateMessageRepository.getHistoryMessageList(userId, friendId, pageIndex, size);
        List<PrivateMessageDTO> collect = list.stream().map(a -> BeanUtils.copyProperties(a, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(collect);
    }

    /**
     * 新增消息
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto) {
        PrivateMessagePO privateMessage = PrivateMessagePO.builder()
                .messageStatus(dto.getMessageStatus())
                .messageContent(dto.getMessageContent())
                .sendId(dto.getSendId())
                .receiverId(dto.getReceiverId())
                .messageContentType(dto.getMessageContentType())
                .sendTime(new Date()).build();
        boolean isSuccess = privateMessageRepository.save(privateMessage);
        return DeveloperResult.success(isSuccess);
    }

    /**
     * 删除消息
     * @param friendId
     * @return
     */
    @Override
    public DeveloperResult<Boolean> deleteMessage(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        boolean isSuccess = privateMessageRepository.deleteChatMessage(userId,friendId);
        return DeveloperResult.success(isSuccess);
    }

    /**
     * 回复消息
     * @param id
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult replyMessage(Long id,SendMessageRequestDTO dto) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(id);
        if(messagePO==null){
            return DeveloperResult.error("回复消息不存在");
        }

        dto.setReferenceId(id);
        this.sendMessage(dto);
        return DeveloperResult.success();
    }

    /**
     * 收藏
     * @param messageId
     * @return
     */
    @Override
    public DeveloperResult collectionMessage(Long messageId) {
        return null;
    }

    /**
     * 转发消息
     * @param messageId
     * @param userIdList
     * @return
     */
    @Override
    public DeveloperResult forwardMessage(Long messageId, List<Long> userIdList) {
        PrivateMessagePO messagePO = privateMessageRepository.getById(messageId);
        if(messagePO==null){
            return DeveloperResult.error("转发消息本体不存在");
        }

        for (Long userId : userIdList) {
            SendMessageRequestDTO dto = SendMessageRequestDTO.builder()
                    .messageContent(messagePO.getMessageContent())
                    .receiverId(userId)
                    .messageContentType(MessageContentTypeEnum.fromCode(messagePO.getMessageContentType()))
                    .messageMainType(MessageMainTypeEnum.PRIVATE_MESSAGE)
                    .build();
            this.sendMessage(dto);
        }
        return DeveloperResult.success();
    }

    private PrivateMessagePO createPrivateMessageMode(Long sendId, Long receiverId, String message, MessageContentTypeEnum messageContentType, MessageStatusEnum messageStatus,Long referenceId){
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
     * 获取最大消息id，没有则修改
     * @param userId
     * @param messageId
     * @return
     */
    private Long fetchAndModifyMessageId(Long userId,Long messageId){
//        String key = RedisKeyConstant.DEVELOPER_MESSAGE_PRIVATE_USER_MAX_ID(userId);
//        Long maxMessageId = (Long) redisTemplate.opsForValue().get(key);
//        if(maxMessageId == null || maxMessageId<messageId){
//            redisTemplate.opsForValue().set(key,messageId,3600, TimeUnit.SECONDS);
//            maxMessageId = messageId;
//        }

        return 0l;
    }
}
