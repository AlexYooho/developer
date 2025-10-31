package com.developer.friend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.friend.client.MessageClient;
import com.developer.friend.client.RpcServiceClient;
import com.developer.friend.dto.*;
import com.developer.friend.enums.*;
import com.developer.friend.pojo.FriendApplyRecordPO;
import com.developer.friend.pojo.FriendPO;
import com.developer.friend.repository.FriendApplyRecordPORepository;
import com.developer.friend.repository.FriendRepository;
import com.developer.friend.service.FriendService;
import com.developer.friend.util.RabbitMQUtil;
import com.developer.rpc.DTO.user.UserInfoRpcDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendApplyRecordPORepository friendApplyRecordPORepository;
    private final MessageClient messageClient;
    private final RabbitMQUtil rabbitMQUtil;
    private final RedisUtil redisUtil;
    private final RpcServiceClient rpcServiceClient;

    @Override
    public DeveloperResult<List<FriendInfoDTO>> findFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();

        // 先去缓存里面查
        String friendsKey = RedisKeyConstant.FRIENDS_KEY(userId);
        String friendsValue = redisUtil.get(friendsKey, String.class);
//        if(StrUtil.isNotBlank(friendsValue)){
//            List<FriendInfoDTO> list = JSON.parseArray(friendsValue, FriendInfoDTO.class);
//            return DeveloperResult.success(serialNo,list);
//        }

        // 查库
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        if(CollUtil.isEmpty(friendList)){
            return DeveloperResult.success(serialNo,new ArrayList<>());
        }

        // 好友id集合
        List<Long> friendIdList = friendList.stream().map(FriendPO::getFriendId).collect(Collectors.toList());

        // 远程调用user rpc服务
        DeveloperResult<List<UserInfoRpcDTO>> userInfoResult = rpcServiceClient.userRpcService.findUserInfo(friendIdList);
        if(!userInfoResult.getIsSuccessful()){
            return DeveloperResult.error(serialNo,userInfoResult.getMsg());
        }

        // 转为map key-value
        Map<Long, UserInfoRpcDTO> userInfoMap = userInfoResult.getData().stream().collect(Collectors.toMap(UserInfoRpcDTO::getUserId, x -> x));

        // 聚合
        List<FriendInfoDTO> list = friendList.stream().map(x -> {
            FriendInfoDTO rep = new FriendInfoDTO();
            rep.setId(x.getFriendId());
            rep.setNickName(x.getFriendNickName());
            rep.setHeadImage(x.getFriendHeadImage());
            rep.setAlias(x.getAlias());
            rep.setTagName(x.getTagName());
            rep.setStatus(x.getStatus());
            rep.setAddSource(x.getAddSource());

            // 从userInfoMap获取附加信息
            UserInfoRpcDTO userInfo = userInfoMap.get(x.getFriendId());
            if(ObjectUtil.isNotEmpty(userInfo)){
                rep.setAccount(userInfo.getAccount());
                rep.setArea(userInfo.getArea());
            }
            return rep;
        }).collect(Collectors.toList());

        // 存入缓存
        redisUtil.set(friendsKey,JSON.toJSON(list),5, TimeUnit.MINUTES);

        return DeveloperResult.success(serialNo, list);
    }

    @Override
    public DeveloperResult<FriendInfoDTO> isFriend(IsFriendDto dto) {
        String serialNo = SerialNoHolder.getSerialNo();
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
    public DeveloperResult<FriendInfoDTO> findFriend(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        DeveloperResult<FriendInfoDTO> friendInfo = this.isFriend(new IsFriendDto(friendId, userId));
        if (!friendInfo.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, friendInfo.getMsg());
        }

        return DeveloperResult.success(serialNo, friendInfo.getData());
    }

    @Override
    public DeveloperResult<Boolean> apply(SendAddFriendInfoRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), userId);
        if (!ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error(serialNo, "对方已是你好友");
        }

        if (Objects.equals(userId, req.getFriendId())) {
            return DeveloperResult.error(serialNo, "不允许添加自己为好友");
        }

        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        req.setRemark("你好,我是" + nickName + ",加个好友呗");
        FriendApplyRecordPO record = friendApplyRecordPORepository.findRecord(req.getFriendId(), userId);
        if (record == null) {
            record = new FriendApplyRecordPO(userId, req.getFriendId(), req.getAddChannel().code(), AddFriendStatusEnum.SENT.code(), new Date(), new Date(), req.getRemark());
            friendApplyRecordPORepository.save(record);
        } else if (record.getStatus().equals(AddFriendStatusEnum.SENT.code()) || record.getStatus().equals(AddFriendStatusEnum.VIEWED.code()) || record.getStatus().equals(AddFriendStatusEnum.REJECTED.code())) {
            friendApplyRecordPORepository.updateStatus(req.getFriendId(), userId, AddFriendStatusEnum.SENT.code());
        } else if (record.getStatus().equals(AddFriendStatusEnum.AGREED.code())) {
            return DeveloperResult.error(serialNo, "已添加该好友,不许重复添加");
        }

        // 发送添加请求
        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, req.getRemark(), Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));
        return DeveloperResult.success(serialNo, true);
    }

    @Override
    public DeveloperResult<Boolean> applyAccept(Long friendId, FriendApplyAcceptDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        if (Objects.equals(userId, friendId)) {
            return DeveloperResult.error(serialNo, "不允许添加自己为好友");
        }

        String message = "我们已经是好友啦";
        // 同意,直接绑定好友关系,发送通知成为好友
        bindFriend(userId, friendId);
        // 新增消息记录
        MessageInsertDTO privateMessage = new MessageInsertDTO();
        privateMessage.setMessageStatus(0);
        privateMessage.setMessageContent("我们已经是好友啦");
        privateMessage.setSendId(userId);
        privateMessage.setReceiverId(friendId);
        privateMessage.setMessageContentType(0);
        privateMessage.setSendTime(new Date());
        privateMessage.setSerialNo(serialNo);
        messageClient.insertMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, privateMessage);

        // 处理请求记录状态
        AddFriendStatusEnum status = AddFriendStatusEnum.AGREED;
        friendApplyRecordPORepository.updateStatus(userId, friendId, status.code());

        rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, message, Collections.singletonList(friendId), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<Boolean> applyReject(Long friendId, FriendApplyRejectDTO dto) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();
        if (Objects.equals(userId, friendId)) {
            return DeveloperResult.error(serialNo, "不允许添加自己为好友");
        }
        // 处理请求记录状态
        AddFriendStatusEnum status = AddFriendStatusEnum.REJECTED;
        friendApplyRecordPORepository.updateStatus(userId, friendId, status.code());
        if (StrUtil.isNotEmpty(dto.getRefuseReason())) {
            rabbitMQUtil.sendMessage(serialNo, DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, userId, nickName, dto.getRefuseReason(), Collections.singletonList(friendId), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));
        }

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<Boolean> deleteFriend(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        FriendPO friend = friendRepository.findByFriendId(friendId, userId);
        if (ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error(serialNo, "对方不是你的好友");
        }

        boolean isSuccess = friendRepository.removeById(friend.getId());
        messageClient.removeFriendChatMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, RemoveMessageRequestDTO.builder().targetId(friendId).serialNo(serialNo).build());

        return DeveloperResult.success(serialNo, isSuccess);
    }

    @Override
    public DeveloperResult<Integer> findFriendAddRequestCount() {
        String serialNo = SerialNoHolder.getSerialNo();
        List<FriendApplyRecordPO> list = friendApplyRecordPORepository.findRecordByStatus(SelfUserInfoContext.selfUserInfo().getUserId(), AddFriendStatusEnum.SENT);
        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<List<NewFriendListDTO>> findNewFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        List<NewFriendListDTO> list = new ArrayList<>();
        return DeveloperResult.success(serialNo, list);
    }

    @Override
    public DeveloperResult<Boolean> updateAddFriendRecordStatus() {
        String serialNo = SerialNoHolder.getSerialNo();
        boolean isSuccess = friendApplyRecordPORepository.updateStatusSentToViewed(SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success(serialNo, isSuccess);
    }

    @Override
    public DeveloperResult<Boolean> batchModifyFriendInfo(BatchModifyFriendListRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
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
