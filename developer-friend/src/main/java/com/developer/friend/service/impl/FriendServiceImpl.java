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
import com.developer.friend.dto.*;
import com.developer.friend.enums.*;
import com.developer.friend.pojo.FriendApplyRecordPO;
import com.developer.friend.pojo.FriendPO;
import com.developer.friend.repository.FriendApplyRecordPORepository;
import com.developer.friend.repository.FriendRepository;
import com.developer.friend.service.FriendService;
import com.developer.friend.util.RabbitMQUtil;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.user.request.UserInfoRequestRpcDTO;
import com.developer.rpc.dto.user.response.UserInfoResponseRpcDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendApplyRecordPORepository friendApplyRecordPORepository;
    private final MessageClient messageClient;
    private final RabbitMQUtil rabbitMQUtil;
    private final RedisUtil redisUtil;
    private final RpcClient rpcClient;

    /*
    获取好友列表
     */
    @Override
    public DeveloperResult<List<FriendInfoDTO>> findFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();

        // 先去缓存里面查
        String friendsKey = RedisKeyConstant.FRIENDS_KEY(userId);
        String friendsValue = redisUtil.get(friendsKey, String.class);
        if (StrUtil.isNotBlank(friendsValue)) {
            List<FriendInfoDTO> list = JSON.parseArray(friendsValue, FriendInfoDTO.class);
            return DeveloperResult.success(serialNo, list);
        }

        // 查库
        List<FriendPO> friendList = friendRepository.findFriendByUserId(userId);
        if (CollUtil.isEmpty(friendList)) {
            return DeveloperResult.success(serialNo, new ArrayList<>());
        }

        // 好友id集合
        List<Long> friendIdList = friendList.stream().map(FriendPO::getFriendId).collect(Collectors.toList());

        // 远程调用user rpc服务
        UserInfoRequestRpcDTO rpcDTO = new UserInfoRequestRpcDTO();
        rpcDTO.setUserIds(friendIdList);
        DeveloperResult<List<UserInfoResponseRpcDTO>> userInfoResult = RpcExecutor.execute(() -> rpcClient.userRpcService.findUserInfo(rpcDTO));
        if (!userInfoResult.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, userInfoResult.getMsg());
        }

        // 转为map key-value
        Map<Long, UserInfoResponseRpcDTO> userInfoMap = userInfoResult.getData().stream().collect(Collectors.toMap(UserInfoResponseRpcDTO::getUserId, x -> x));

        // 聚合
        List<FriendInfoDTO> list = friendList.stream().map(x -> {
            FriendInfoDTO rep = new FriendInfoDTO();
            rep.setId(x.getFriendId());
            rep.setAlias(x.getAlias());
            rep.setTagName(x.getTagName());
            rep.setStatus(x.getStatus());
            rep.setAddSource(x.getAddSource());

            // 从userInfoMap获取附加信息
            UserInfoResponseRpcDTO userInfo = userInfoMap.get(x.getFriendId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                rep.setAccount(userInfo.getAccount());
                rep.setNickName(userInfo.getNickName());
                rep.setHeadImage(userInfo.getAvatar());
                rep.setHeadImageThumb(userInfo.getAvatarThumb());
                rep.setArea(userInfo.getArea());
                rep.setUserName(userInfo.getUserName());
                rep.setSex(userInfo.getSex());
            }
            return rep;
        }).collect(Collectors.toList());

        // 存入缓存
        redisUtil.set(friendsKey, JSON.toJSON(list), 5, TimeUnit.MINUTES);

        return DeveloperResult.success(serialNo, list);
    }

    /*
    是否为好友
     */
    @Override
    public DeveloperResult<FriendInfoDTO> isFriend(IsFriendDto dto) {
        dto.setUserId(SelfUserInfoContext.selfUserInfo().getUserId());
        // 1、先去缓存里面获取
        String friendsKey = RedisKeyConstant.FRIENDS_KEY(dto.getUserId());
        String friendsValue = redisUtil.get(friendsKey, String.class);
        if (StrUtil.isNotBlank(friendsValue)) {
            List<FriendInfoDTO> list = JSON.parseArray(friendsValue, FriendInfoDTO.class);
            FriendInfoDTO friendInfoDTO = list.stream().filter(x -> x.getId().equals(dto.getFriendId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(friendInfoDTO)) {
                return DeveloperResult.success(SerialNoHolder.getSerialNo(), friendInfoDTO);
            }
        }

        // 缓存没找到，再去查库
        FriendPO friend = friendRepository.findByFriendId(dto.getFriendId(), dto.getUserId());
        if (friend == null) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "对方不是你的好友");
        }

        FriendInfoDTO friendInfoDTO = new FriendInfoDTO();
        friendInfoDTO.setId(friend.getId());
        friendInfoDTO.setAlias(friend.getAlias());
        friendInfoDTO.setTagName(friend.getTagName());
        friendInfoDTO.setStatus(friend.getStatus());
        friendInfoDTO.setAddSource(friend.getAddSource());

        // 远程调用user rpc服务
        UserInfoRequestRpcDTO rpcDTO = new UserInfoRequestRpcDTO();
        rpcDTO.setUserIds(Collections.singletonList(friend.getUserId()));
        DeveloperResult<List<UserInfoResponseRpcDTO>> userInfoResult = RpcExecutor.execute(() -> rpcClient.userRpcService.findUserInfo(rpcDTO));
        if (!userInfoResult.getIsSuccessful()) {
            log.warn("【好友服务】serialNo：{},获取好友用户信息失败,user_id:{},friend_id:{},失败原因:{}",SerialNoHolder.getSerialNo(),dto.getUserId(),dto.getFriendId(),userInfoResult.getMsg());
        } else {
            UserInfoResponseRpcDTO userInfoRpcDTO = userInfoResult.getData().stream().findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(userInfoRpcDTO)) {
                friendInfoDTO.setAccount(userInfoRpcDTO.getAccount());
                friendInfoDTO.setNickName(userInfoRpcDTO.getNickName());
                friendInfoDTO.setHeadImage(userInfoRpcDTO.getAvatar());
                friendInfoDTO.setHeadImageThumb(userInfoRpcDTO.getAvatarThumb());
                friendInfoDTO.setArea(userInfoRpcDTO.getArea());
                friendInfoDTO.setUserName(userInfoRpcDTO.getUserName());
                friendInfoDTO.setSex(userInfoRpcDTO.getSex());
            }
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), friendInfoDTO);
    }

    /*
    查询好友信息
     */
    @Override
    public DeveloperResult<FriendInfoDTO> findFriend(Long friendId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        DeveloperResult<FriendInfoDTO> friendInfo = this.isFriend(new IsFriendDto(friendId, userId));
        if (!friendInfo.getIsSuccessful()) {
            return DeveloperResult.error(serialNo, friendInfo.getMsg());
        }

        return friendInfo;
    }

    /*
    发送成为好友申请
     */
    @Override
    public DeveloperResult<Boolean> apply(SendAddFriendInfoRequestDTO req) {

        // 是否自己加自己
        if (Objects.equals(SelfUserInfoContext.selfUserInfo().getUserId(), req.getFriendId())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不允许添加自己为好友");
        }

        // 是否已存在好友关系
        FriendPO friend = friendRepository.findByFriendId(req.getFriendId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if (!ObjectUtil.isEmpty(friend)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "对方已是你好友");
        }

        // 检测好友用户是否存在
        UserInfoRequestRpcDTO friendUserInfoRequestDTO = new UserInfoRequestRpcDTO();
        friendUserInfoRequestDTO.setUserIds(Collections.singletonList(req.getFriendId()));
        DeveloperResult<List<UserInfoResponseRpcDTO>> userRpcResponse = RpcExecutor.execute(() -> rpcClient.userRpcService.findUserInfo(friendUserInfoRequestDTO));
        if(!userRpcResponse.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),userRpcResponse.getMsg());
        }

        UserInfoResponseRpcDTO friendUserInfo = userRpcResponse.getData().stream().findFirst().orElse(null);
        if(ObjectUtil.isEmpty(friendUserInfo)){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),"用户不存在,无法申请好友");
        }

        // 申请记录入库
        req.setRemark("你好,我是" + SelfUserInfoContext.selfUserInfo().getNickName() + ",加个好友呗");
        FriendApplyRecordPO record = friendApplyRecordPORepository.findRecord(req.getFriendId(), SelfUserInfoContext.selfUserInfo().getUserId());
        if (record == null) {
            record = new FriendApplyRecordPO(SelfUserInfoContext.selfUserInfo().getUserId(), req.getFriendId(), req.getAddChannel().code(), AddFriendStatusEnum.SENT.code(), new Date(), new Date(), req.getRemark());
            friendApplyRecordPORepository.save(record);
        } else if (record.getStatus().equals(AddFriendStatusEnum.SENT.code()) || record.getStatus().equals(AddFriendStatusEnum.VIEWED.code()) || record.getStatus().equals(AddFriendStatusEnum.REJECTED.code())) {
            friendApplyRecordPORepository.updateStatus(req.getFriendId(), SelfUserInfoContext.selfUserInfo().getUserId(), AddFriendStatusEnum.SENT.code());
        } else if (record.getStatus().equals(AddFriendStatusEnum.AGREED.code())) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "已添加该好友,不许重复添加");
        }

        // 发送添加请求
        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), req.getRemark(), Collections.singletonList(req.getFriendId()), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    接受好友申请
     */
    @Override
    public DeveloperResult<Boolean> applyAccept(Long friendId, FriendApplyAcceptDTO dto) {
        if (Objects.equals(SelfUserInfoContext.selfUserInfo().getUserId(), friendId)) {
            return DeveloperResult.error(SerialNoHolder.getSerialNo(), "不允许添加自己为好友");
        }

        String message = "我们已经是好友啦";
        // 同意,直接绑定好友关系,发送通知成为好友
        bindFriend(SelfUserInfoContext.selfUserInfo().getUserId(), friendId);
        // 新增消息记录
        MessageInsertDTO privateMessage = new MessageInsertDTO();
        privateMessage.setMessageStatus(0);
        privateMessage.setMessageContent(message);
        privateMessage.setSendId(SelfUserInfoContext.selfUserInfo().getUserId());
        privateMessage.setReceiverId(friendId);
        privateMessage.setMessageContentType(0);
        privateMessage.setSendTime(new Date());
        privateMessage.setSerialNo(SerialNoHolder.getSerialNo());
        messageClient.insertMessage(MessageMainTypeEnum.PRIVATE_MESSAGE, privateMessage);

        rabbitMQUtil.sendMessage(SerialNoHolder.getSerialNo(), DeveloperMQConstant.MESSAGE_IM_EXCHANGE, DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY, ProcessorTypeEnum.IM, builderMQMessageDTO(MessageMainTypeEnum.SYSTEM_MESSAGE, MessageContentTypeEnum.TEXT, 0L, 0L, SelfUserInfoContext.selfUserInfo().getUserId(), SelfUserInfoContext.selfUserInfo().getNickName(), message, Collections.singletonList(friendId), new ArrayList<>(), MessageStatusEnum.UNSEND, MessageTerminalTypeEnum.WEB, new Date()));

        // 推送同意好友申请消息
        DeveloperResult<Boolean> executeResult = RpcExecutor.execute(() -> rpcClient.messageRpcService.sendFriendApplyAcceptMessage());
        if(!executeResult.getIsSuccessful()){
            return DeveloperResult.error(SerialNoHolder.getSerialNo(),executeResult.getMsg());
        }

        // 处理请求记录状态
        friendApplyRecordPORepository.updateStatus(SelfUserInfoContext.selfUserInfo().getUserId(), friendId, AddFriendStatusEnum.AGREED.code());

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }

    /*
    拒绝好友申请
     */
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

    /*
    删除好友
     */
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

    /*
    获取好友申请数
     */
    @Override
    public DeveloperResult<Integer> findFriendAddRequestCount() {
        String serialNo = SerialNoHolder.getSerialNo();
        List<FriendApplyRecordPO> list = friendApplyRecordPORepository.findRecordByStatus(SelfUserInfoContext.selfUserInfo().getUserId(), AddFriendStatusEnum.SENT);
        return DeveloperResult.success(serialNo);
    }

    /*
    获取好友申请列表
     */
    @Override
    public DeveloperResult<List<NewFriendListDTO>> findNewFriendList() {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String serialNo = SerialNoHolder.getSerialNo();
        List<NewFriendListDTO> list = new ArrayList<>();
        return DeveloperResult.success(serialNo, list);
    }

    /*
    更改好友申请状态
     */
    @Override
    public DeveloperResult<Boolean> updateAddFriendRecordStatus() {
        String serialNo = SerialNoHolder.getSerialNo();
        boolean isSuccess = friendApplyRecordPORepository.updateStatusSentToViewed(SelfUserInfoContext.selfUserInfo().getUserId());
        return DeveloperResult.success(serialNo, isSuccess);
    }

    /*
    批量修改好友信息
     */
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
