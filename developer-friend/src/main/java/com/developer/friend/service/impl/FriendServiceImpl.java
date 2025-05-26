package com.developer.friend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.friend.client.MessageClient;
import com.developer.friend.dto.*;
import com.developer.friend.enums.*;
import com.developer.friend.pojo.FriendApplicationRecordPO;
import com.developer.friend.pojo.FriendPO;
import com.developer.friend.repository.FriendApplicationRecordPORepository;
import com.developer.friend.repository.FriendRepository;
import com.developer.friend.service.FriendService;
import com.developer.friend.util.RabbitMQUtil;
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
    private RabbitMQUtil rabbitMQUtil;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public DeveloperResult<List<FriendInfoDTO>> findFriendList(String serialNo) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        serialNo = snowflakeNoUtil.getSerialNo(serialNo);
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        List<FriendInfoDTO> list = friendList.stream().map(x -> {
            FriendInfoDTO rep = new FriendInfoDTO();
            rep.setId(x.getFriendId());
            rep.setNickName(x.getFriendNickName());
            rep.setHeadImage(x.getFriendHeadImage());
            return rep;
        }).collect(Collectors.toList());
        return DeveloperResult.success(serialNo, list);
    }

    @Override
    public DeveloperResult<FriendInfoDTO> isFriend(IsFriendDto dto) {
        String serialNo = snowflakeNoUtil.getSerialNo(dto.getSerialNo());
        FriendPO friend = friendRepository.findByFriendId(dto.getFriendId(), dto.getUserId());
        if (friend == null) {
            return DeveloperResult.error(serialNo, "对方不是你的好友");
        }

        FriendInfoDTO friendInfoDTO = new FriendInfoDTO();
        friendInfoDTO.setId(friend.getId());
        friendInfoDTO.setHeadImage(friend.getFriendHeadImage());
        friendInfoDTO.setNickName(friend.getFriendNickName());
        return DeveloperResult.success(serialNo, friendInfoDTO);
    }

    @Override
    public DeveloperResult<FriendInfoDTO> findFriend(FindFriendRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        DeveloperResult<FriendInfoDTO> friendInfo = this.isFriend(new IsFriendDto(serialNo, req.getFriendId(), userId));
        if (!friendInfo.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, friendInfo.getMsg());
        }

        return DeveloperResult.success(serialNo, friendInfo.getData());
    }

    @Override
    public DeveloperResult<Boolean> sendAddFriendRequest(SendAddFriendInfoRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), userId);
        if (!ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error(serialNo, "对方已是你好友");
        }

        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error(serialNo, "不允许添加自己为好友");
        }

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        req.setRemark("你好,我是" + nickName + ",加个好友呗");
        FriendApplicationRecordPO record = friendApplicationRecordPORepository.findRecord(req.getFriendId(), userId);
        if (record == null) {
            record = new FriendApplicationRecordPO(userId, req.getFriendId(), req.getAddChannel().code(), AddFriendStatusEnum.SENT.code(), new Date(), new Date(), req.getRemark());
            friendApplicationRecordPORepository.save(record);
        } else if (record.getStatus().equals(AddFriendStatusEnum.SENT.code()) || record.getStatus().equals(AddFriendStatusEnum.VIEWED.code()) || record.getStatus().equals(AddFriendStatusEnum.REJECTED.code())) {
            friendApplicationRecordPORepository.updateStatus(req.getFriendId(), userId, AddFriendStatusEnum.SENT.code());
        } else if (record.getStatus().equals(AddFriendStatusEnum.AGREED.code())) {
            return DeveloperResult.error(serialNo, "已添加该好友,不许重复添加");
        }

        // 发送添加请求
        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, req.getRemark(), Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));
        return DeveloperResult.success(serialNo, true);
    }

    @Override
    public DeveloperResult<Boolean> processFriendRequest(ProcessAddFriendRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error(serialNo, "不允许添加自己为好友");
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
            privateMessage.setSerialNo(serialNo);
            messageClient.insertMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, privateMessage);
        } else {
            // 拒绝,如果拒绝理由不为空则回复消息
            message = req.getRefuseReason();
        }

        // 处理请求记录状态
        AddFriendStatusEnum status = req.getIsAgree() ? AddFriendStatusEnum.AGREED : AddFriendStatusEnum.REJECTED;
        friendApplicationRecordPORepository.updateStatus(userId, req.getFriendId(), status.code());

        if (ObjectUtil.isNotEmpty(message)) {
            rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, message, Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));
        }

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<Boolean> deleteFriendByFriendId(DeleteFriendRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), userId);
        if (ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error(serialNo, "对方不是你的好友");
        }

        boolean isSuccess = friendRepository.removeById(friend.getId());
        messageClient.removeFriendChatMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, RemoveMessageRequestDTO.builder().targetId(req.getFriendId()).serialNo(serialNo).build());

        return DeveloperResult.success(serialNo, isSuccess);
    }

    @Override
    public DeveloperResult<Integer> findFriendAddRequestCount(String serialNo) {
        serialNo = snowflakeNoUtil.getSerialNo(serialNo);
        List<FriendApplicationRecordPO> list = friendApplicationRecordPORepository.findRecordByStatus(SelfUserInfoContext.selfUserInfo().getUserId(), AddFriendStatusEnum.SENT);
        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<List<NewFriendListDTO>> findNewFriendList(String serialNo) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        serialNo = serialNo.isEmpty() ? snowflakeNoUtil.getSerialNo() : serialNo;
        List<NewFriendListDTO> list = new ArrayList<>();
        return DeveloperResult.success(serialNo, list);
    }

    @Override
    public DeveloperResult<Boolean> updateAddFriendRecordStatus(String serialNo) {
        serialNo = snowflakeNoUtil.getSerialNo(serialNo);
        boolean isSuccess = friendApplicationRecordPORepository.updateStatusSentToViewed(SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success(serialNo, isSuccess);
    }

    @Override
    public DeveloperResult<Boolean> modifyFriendList(BatchModifyFriendListRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        List<FriendPO> friendPOS = BeanUtils.copyProperties(req.getList(), FriendPO.class);
        boolean isSuccess = friendRepository.updateBatchById(friendPOS);
        if (!isSuccess) {
            return DeveloperResult.error(serialNo, "修改失败");
        }
        return DeveloperResult.success(serialNo);
    }

    /**
     * 绑定好友关系
     *
     * @param userId
     * @param friendId
     */
    public void bindFriend(Long userId, Long friendId) {
    }

    private RabbitMQMessageBodyDTO builderMQMessageDTO(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, MessageStatusEnum messageStatus, MessageTerminalTypeEnum terminalType, Date sendTime) {
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
}
