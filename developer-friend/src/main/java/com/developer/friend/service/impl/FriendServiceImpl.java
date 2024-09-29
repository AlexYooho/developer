package com.developer.friend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.MQMessageDTO;
import com.developer.framework.dto.MessageDTO;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.friend.client.MessageClient;
import com.developer.friend.dto.*;
import com.developer.friend.enums.*;
import com.developer.friend.pojo.FriendApplicationRecordPO;
import com.developer.friend.pojo.FriendPO;
import com.developer.friend.repository.FriendApplicationRecordPORepository;
import com.developer.friend.repository.FriendRepository;
import com.developer.friend.service.FriendService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendApplicationRecordPORepository friendApplicationRecordPORepository;

    @Autowired
    private MessageClient messageClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public DeveloperResult<List<FriendInfoDTO>> findFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        List<FriendInfoDTO> list = friendList.stream().map(x -> {
            FriendInfoDTO rep = new FriendInfoDTO();
            rep.setId(x.getFriendId());
            rep.setNickName(x.getFriendNickName());
            rep.setHeadImage(x.getFriendHeadImage());
            return rep;
        }).collect(Collectors.toList());
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult<Boolean> isFriend(Long userId1, Long userId2) {
        boolean result = friendRepository.isFriend(userId1,userId2);
        return DeveloperResult.success(result);
    }

    @Override
    public DeveloperResult<FriendInfoDTO> findFriend(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(friendId, userId);
        if (friend == null) {
            return DeveloperResult.error("对方不是你的好友");
        }

        FriendInfoDTO friendInfoDTO = new FriendInfoDTO();
        friendInfoDTO.setId(friend.getId());
        friendInfoDTO.setHeadImage(friend.getFriendHeadImage());
        friendInfoDTO.setNickName(friend.getFriendNickName());
        return DeveloperResult.success(friendInfoDTO);
    }

    @Override
    public DeveloperResult<Boolean> sendAddFriendRequest(SendAddFriendInfoRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), userId);
        if (!ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error("对方已是你好友");
        }

        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error("不允许添加自己为好友");
        }

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        req.setRemark("你好,我是"+nickName+",加个好友呗");
        FriendApplicationRecordPO record = friendApplicationRecordPORepository.findRecord(req.getFriendId(), userId);
        if (record == null) {
            record = new FriendApplicationRecordPO(userId, req.getFriendId(), req.getAddChannel().code(), AddFriendStatusEnum.SENT.code(), new Date(), new Date(), req.getRemark());
            friendApplicationRecordPORepository.save(record);
        } else if (record.getStatus().equals(AddFriendStatusEnum.SENT.code()) || record.getStatus().equals(AddFriendStatusEnum.VIEWED.code()) || record.getStatus().equals(AddFriendStatusEnum.REJECTED.code())) {
            friendApplicationRecordPORepository.updateStatus(req.getFriendId(), userId, AddFriendStatusEnum.SENT.code());
        } else if (record.getStatus().equals(AddFriendStatusEnum.AGREED.code())) {
            return DeveloperResult.error("已添加该好友,不许重复添加");
        }

        // 发送添加请求
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_EXCHANGE,DeveloperMQConstant.CHAT_MESSAGE_ROUTING_KEY, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, req.getRemark(), Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, IMTerminalTypeEnum.WEB, new Date()));
        return DeveloperResult.success(true);
    }

    @Override
    public DeveloperResult<Boolean> processFriendRequest(ProcessAddFriendRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error("不允许添加自己为好友");
        }

        String message = "";
        if (req.getIsAgree()) {
            // 同意,直接绑定好友关系,发送通知成为好友
            bindFriend(userId, req.getFriendId());
            // 发送添加请求
            message = "我们已经是好友啦";
            // 新增消息记录
            MessageInsertDTO privateMessage = new MessageInsertDTO();
            privateMessage.setMessageStatus(0);
            privateMessage.setMessageContent("我们已经是好友啦");
            privateMessage.setSendId(userId);
            privateMessage.setReceiverId(req.getFriendId());
            privateMessage.setMessageContentType(0);
            privateMessage.setSendTime(new Date());
            messageClient.insertMessage(MessageMainTypeEnum.PRIVATE_MESSAGE.code(),privateMessage);
        } else {
            // 拒绝,如果拒绝理由不为空则回复消息
            message = req.getRefuseReason();
        }

        // 处理请求记录状态
        AddFriendStatusEnum status = req.getIsAgree() ? AddFriendStatusEnum.AGREED : AddFriendStatusEnum.REJECTED;
        friendApplicationRecordPORepository.updateStatus(userId, req.getFriendId(), status.code());

        if (ObjectUtil.isNotEmpty(message)) {
            rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_EXCHANGE,DeveloperMQConstant.CHAT_MESSAGE_ROUTING_KEY, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, message, Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, IMTerminalTypeEnum.WEB, new Date()));
        }

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult<Boolean> deleteFriendByFriendId(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        FriendPO friend = friendRepository.findByFriendId(friendId, userId);
        if (ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error("对方不是你的好友");
        }

        boolean isSuccess = friendRepository.removeById(friend.getId());
        messageClient.removeFriendChatMessage(MessageMainTypeEnum.PRIVATE_MESSAGE.code(),friendId);

        return DeveloperResult.success(isSuccess);
    }

    @Override
    public DeveloperResult<Integer> findFriendAddRequestCount() {
        List<FriendApplicationRecordPO> list = friendApplicationRecordPORepository.findRecordByStatus(SelfUserInfoContext.selfUserInfo().getUserId(),AddFriendStatusEnum.SENT);
        return DeveloperResult.success(list.size());
    }

    @Override
    public DeveloperResult<List<NewFriendListDTO>> findNewFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        //List<NewFriendListDTO> lists = friendApplicationRecordPORepository.findNewFriendList(userId);
        List<NewFriendListDTO> list = new ArrayList<>();
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult<Boolean> updateAddFriendRecordStatus() {
        boolean isSuccess = friendApplicationRecordPORepository.updateStatusSentToViewed(SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success(isSuccess);
    }

    @Override
    public DeveloperResult<Boolean> modifyFriendList(List<FriendInfoDTO> list) {
        List<FriendPO> friendPOS = BeanUtils.copyProperties(list, FriendPO.class);
        boolean isSuccess = friendRepository.updateBatchById(friendPOS);
        return DeveloperResult.success(isSuccess);
    }

    /**
     * 绑定好友关系
     *
     * @param userId
     * @param friendId
     */
    public void bindFriend(Long userId, Long friendId) {
    }

    private MQMessageDTO<MessageDTO> builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, MessageStatusEnum messageStatus, IMTerminalTypeEnum terminalType, Date sendTime){
        return MQMessageDTO.<MessageDTO>builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .data(MessageDTO.builder().messageMainTypeEnum(messageMainTypeEnum)
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
                        .sendTime(sendTime).build())
                .build();
    }
}
