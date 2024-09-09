package com.developer.message.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.message.client.FriendClient;
import com.developer.message.dto.FriendInfoDTO;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.PrivateMessageDTO;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.pojo.PrivateMessagePO;
import com.developer.message.repository.PrivateMessageRepository;
import com.developer.message.service.MessageService;
import com.developer.message.util.RabbitMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Override
    public DeveloperResult loadMessage(Long minId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        DeveloperResult friends = friendClient.friends();
        if(ObjectUtil.isEmpty(friends.getData())){
            return DeveloperResult.success();
        }

        List<FriendInfoDTO> list2 = (List<FriendInfoDTO>) friends.getData();

        List<Long> friendIds = list2.stream().map(FriendInfoDTO::getId).collect(Collectors.toList());
        List<PrivateMessagePO> messages = privateMessageRepository.findCurrentUserMessage(minId, userId, friendIds);

        List<Long> ids = messages.stream().filter(x -> !x.getSendId().equals(userId) && x.getMessageStatus().equals(MessageStatusEnum.UNSEND.code()))
                .map(PrivateMessagePO::getId)
                .collect(Collectors.toList());
        if(!ids.isEmpty()){
            LambdaUpdateWrapper<PrivateMessagePO> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(PrivateMessagePO::getId, ids)
                    .set(PrivateMessagePO::getMessageStatus, MessageStatusEnum.SENDED.code());
            this.privateMessageRepository.update(updateWrapper);
        }

        log.info("拉取消息,用户id:{},数量:{}", userId, messages.size());
        List<PrivateMessageDTO> list = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageDTO.class)).collect(Collectors.toList());

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
        DeveloperResult friend = friendClient.isFriend(userId, req.getReceiverId());
        boolean isFriend = (boolean) friend.getData();
        if(!isFriend){
            return DeveloperResult.error("对方不是你好友,无法发送消息");
        }

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();

        // 消息入库
        PrivateMessagePO privateMessage = createPrivateMessageMode(userId, req.getReceiverId(), req.getMessageContent(), req.getMessageContentType(), MessageStatusEnum.SENDED);
        this.privateMessageRepository.save(privateMessage);
        rabbitMQUtil.pushMQMessage(req.getMessageMainType(),req.getMessageContentType(), privateMessage.getId(), 0L, userId, nickName, req.getMessageContent(), Arrays.asList(req.getReceiverId()),new ArrayList<>(), privateMessage.getMessageStatus(), IMTerminalTypeEnum.WEB,privateMessage.getSendTime());

        return DeveloperResult.success(privateMessage.getId());
    }

    @Override
    public DeveloperResult readMessage(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, "",Arrays.asList(friendId),new ArrayList<>(), MessageStatusEnum.READED.code(), IMTerminalTypeEnum.WEB,new Date());
        privateMessageRepository.UpdateStatus(friendId,userId,MessageStatusEnum.READED.code());
        return DeveloperResult.success();
    }

    /**
     * 撤回消息
     * @param id
     * @return
     */
    @Override
    public DeveloperResult recallMessage(Long id) {
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

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        // 修改消息状态
        privateMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        privateMessageRepository.updateById(privateMessage);

        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, MessageContentTypeEnum.TEXT, privateMessage.getId(), 0L, userId, nickName, "对方撤回了一条消息",Arrays.asList(privateMessage.getReceiverId()),new ArrayList<>(), MessageStatusEnum.RECALL.code(), IMTerminalTypeEnum.WEB,new Date());
        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult findHistoryMessage(Long friendId, Long page, Long size) {
        page = page>0?page:1;
        size = size>0?size:10;
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        long pageIndex = (page-1)*size;
        List<PrivateMessagePO> list = privateMessageRepository.findPrivateMessageList(userId, friendId, pageIndex, size);
        List<PrivateMessageDTO> collect = list.stream().map(a -> BeanUtils.copyProperties(a, PrivateMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(collect);
    }

    @Override
    public DeveloperResult insertMessage(MessageInsertDTO dto) {
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setMessageStatus(0);
        privateMessage.setMessageContent("我们已经是好友啦");
        privateMessage.setSendId(dto.getSendId());
        privateMessage.setReceiverId(dto.getReceiverId());
        privateMessage.setMessageContentType(0);
        privateMessage.setSendTime(new Date());
        privateMessageRepository.save(privateMessage);
        return DeveloperResult.success();
    }

    private PrivateMessagePO createPrivateMessageMode(Long sendId, Long receiverId, String message, MessageContentTypeEnum messageContentType, MessageStatusEnum messageStatus){
        PrivateMessagePO privateMessage = new PrivateMessagePO();
        privateMessage.setSendId(sendId);
        privateMessage.setReceiverId(receiverId);
        privateMessage.setMessageContent(message);
        privateMessage.setMessageContentType(messageContentType.code());
        privateMessage.setMessageStatus(messageStatus.code());
        privateMessage.setSendTime(new Date());
        return privateMessage;
    }
}
