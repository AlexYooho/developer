package com.developer.message.service.impl;

import cn.hutool.core.util.StrUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.MessageStatusEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.message.client.GroupInfoClient;
import com.developer.message.client.GroupMemberClient;
import com.developer.message.dto.*;
import com.developer.message.pojo.GroupMessageMemberReceiveRecordPO;
import com.developer.message.pojo.GroupMessagePO;
import com.developer.message.repository.GroupMessageMemberReceiveRecordRepository;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.service.MessageService;
import com.developer.message.util.RabbitMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupMessageServiceImpl implements MessageService {

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private GroupInfoClient groupInfoClient;

    @Autowired
    private GroupMessageMemberReceiveRecordRepository groupMessageMemberReceiveRecordRepository;

    @Autowired
    private GroupMemberClient groupMemberClient;

    @Override
    public DeveloperResult loadMessage(Long minId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        DeveloperResult developerResult = groupInfoClient.getSelfJoinAllGroupInfo();
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = (List<SelfJoinGroupInfoDTO>) developerResult.getData();
        if(joinGroupInfoList.isEmpty()){
            return DeveloperResult.success();
        }

        List<Long> groupIds = joinGroupInfoList.stream().map(SelfJoinGroupInfoDTO::getUserId).collect(Collectors.toList());

        // 当前用户有多少群消息未读
        List<GroupMessageMemberReceiveRecordPO> unreadMessageList = groupMessageMemberReceiveRecordRepository.findAllUnreadMessageList(userId);
        // 当前用户发送的群消息有多少已读未读
        List<GroupMessageMemberReceiveRecordPO> curUserSendMessageList = groupMessageMemberReceiveRecordRepository.findAllMessageBySendId(userId);

        Date minDate = DateTimeUtils.addMonths(new Date(), -3);
        List<GroupMessagePO> messages = groupMessageRepository.find(minId, minDate, groupIds);
        List<GroupMessageDTO> vos = messages.stream().map(x -> {
            GroupMessageDTO vo = BeanUtils.copyProperties(x, GroupMessageDTO.class);
            Integer messageStatus = unreadMessageList.stream().anyMatch(z -> {
                return Objects.equals(z.getGroupId(), x.getGroupId()) && Objects.equals(z.getMessageId(), x.getId());
            }) ? MessageStatusEnum.UNSEND.code() : MessageStatusEnum.READED.code();
            assert vo != null;
            vo.setMessageStatus(messageStatus);

            if(vo.getSendId().equals(userId)){
                long unReadCount = curUserSendMessageList.stream().filter(m-> Objects.equals(m.getMessageId(), vo.getId()) && m.getStatus()==0).collect(Collectors.toList()).size();
                long readCount = curUserSendMessageList.stream().filter(m-> Objects.equals(m.getMessageId(), vo.getId()) && m.getStatus()==3).collect(Collectors.toList()).size();
                vo.setUnReadCount(unReadCount);
                vo.setReadCount(readCount);
            }
            return vo;
        }).collect(Collectors.toList());

        return DeveloperResult.success(vos);
    }

    @Override
    public DeveloperResult sendMessage(SendMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();

        DeveloperResult developerResult = groupInfoClient.findGroup(req.getGroupId());
        GroupInfoDTO groupInfoDTO = (GroupInfoDTO) developerResult.getData();
        if(Objects.isNull(groupInfoDTO)){
            return DeveloperResult.error("群聊不存在");
        }

        if(groupInfoDTO.getDeleted()){
            return DeveloperResult.error("群已解散");
        }

        DeveloperResult developerResult2 = groupInfoClient.getSelfJoinAllGroupInfo();
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = (List<SelfJoinGroupInfoDTO>) developerResult2.getData();
        if(joinGroupInfoList.stream().noneMatch(x -> x.getGroupId().equals(req.getGroupId()) && x.getQuit())){
            return DeveloperResult.error("您已不在该群聊中,无法发送消息");
        }

        // 消息入库
        GroupMessagePO message = this.createGroupMessageMode( req.getGroupId(), userId, nickName, req.getAtUserIds(), req.getMessageContent(), req.getMessageContentType());
        this.groupMessageRepository.save(message);

        // 需要接受消息的成员
        List<Long> receiverIds = (List<Long>)groupMemberClient.findGroupMemberUserId(groupInfoDTO.getId()).getData();
        receiverIds = receiverIds.stream().filter(id->!userId.equals(id)).collect(Collectors.toList());

        List<GroupMessageMemberReceiveRecordPO> receiveRecods = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            GroupMessageMemberReceiveRecordPO recod = new GroupMessageMemberReceiveRecordPO();
            recod.setGroupId(req.getGroupId());
            recod.setReceiverId(receiverId);
            recod.setStatus(0);
            recod.setCreateTime(new Date());
            recod.setUpdateTime(new Date());
            recod.setSendId(userId);
            recod.setMessageId(message.getId());
            receiveRecods.add(recod);
        }

        groupMessageMemberReceiveRecordRepository.saveBatch(receiveRecods);

        rabbitMQUtil.pushMQMessage(req.getMessageMainType(),req.getMessageContentType(),message.getId(),message.getGroupId(),userId,nickName, req.getMessageContent(),receiverIds,req.getAtUserIds(),message.getMessageStatus(),IMTerminalTypeEnum.WEB,message.getSendTime());

        GroupMessageDTO data = new GroupMessageDTO();
        data.setId(message.getId());
        data.setReadCount(0L);
        data.setUnReadCount((long) receiverIds.size());
        return DeveloperResult.success(data);
    }

    @Override
    public DeveloperResult readMessage(Long groupId) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String nickName = SelfUserInfoContext.selfUserInfo().getNickName();

        GroupMessagePO lastMessage = groupMessageRepository.findLastMessage(groupId);
        if(Objects.isNull(lastMessage)){
            return DeveloperResult.success();
        }

        // 修改群已读状态
        List<GroupMessageMemberReceiveRecordPO> records = groupMessageMemberReceiveRecordRepository.findCurGroupUnreadRecordList(groupId, userId);
        records.forEach(x->{
            // 通知前端
            rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.GROUP_MESSAGE,MessageContentTypeEnum.TEXT, x.getMessageId(), groupId, userId, nickName, "", Collections.singletonList(x.getSendId()), new ArrayList<>(), 3, IMTerminalTypeEnum.WEB,new Date());
            x.setStatus(MessageStatusEnum.READED.code());
            groupMessageMemberReceiveRecordRepository.updateById(x);
        });

        String key = StrUtil.join(",", RedisKeyConstant.IM_GROUP_READED_POSITION,groupId,userId);
        redisTemplate.opsForValue().set(key,lastMessage.getId());
        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult recallMessage(Long id) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        GroupMessagePO groupMessage = groupMessageRepository.getById(id);
        if(groupMessage==null){
            return DeveloperResult.error("消息不存在");
        }

        if(!groupMessage.getSendId().equals(userId)){
            return DeveloperResult.error("无法撤回不是自己发送的消息");
        }

        DeveloperResult developerResult = groupInfoClient.getSelfJoinAllGroupInfo();
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = (List<SelfJoinGroupInfoDTO>) developerResult.getData();
        SelfJoinGroupInfoDTO selfJoinGroupInfoDTO = joinGroupInfoList.stream().filter(x -> x.getGroupId().equals(groupMessage.getGroupId()) && x.getQuit()).findFirst().get();
        if(selfJoinGroupInfoDTO==null){
            return DeveloperResult.error("您已不在该群聊中,无法撤回消息");
        }

        groupMessage.setMessageStatus(MessageStatusEnum.RECALL.code());
        groupMessageRepository.updateById(groupMessage);

        List<Long> receiverIds = (List<Long>)groupMemberClient.findGroupMemberUserId(groupMessage.getId()).getData();
        receiverIds = receiverIds.stream().filter(x->!userId.equals(x)).collect(Collectors.toList());

        String message = String.format("%s 撤回了一条消息",selfJoinGroupInfoDTO.getAliasName());

        rabbitMQUtil.pushMQMessage(MessageMainTypeEnum.GROUP_MESSAGE, MessageContentTypeEnum.TEXT, groupMessage.getId(), groupMessage.getGroupId(), groupMessage.getSendId(), groupMessage.getSendNickName(), message,receiverIds,new ArrayList<>(), groupMessage.getMessageStatus(), IMTerminalTypeEnum.WEB,new Date());

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult findHistoryMessage(Long groupId, Long page, Long size) {
        page = page>0?page:1;
        size = size>0?size:10;
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        long stIdx = (page-1)*size;

        DeveloperResult developerResult = groupInfoClient.getSelfJoinAllGroupInfo();
        List<SelfJoinGroupInfoDTO> joinGroupInfoList = (List<SelfJoinGroupInfoDTO>) developerResult.getData();
        SelfJoinGroupInfoDTO selfJoinGroupInfoDTO = joinGroupInfoList.stream().filter(x -> x.getGroupId().equals(groupId) && x.getQuit()).findFirst().get();
        if(selfJoinGroupInfoDTO==null){
            return DeveloperResult.error("您已不在群聊");
        }

        List<GroupMessagePO> messages = groupMessageRepository.findHistoryMessage(groupId, selfJoinGroupInfoDTO.getCreatedTime(), stIdx, size);
        List<GroupMessageDTO> list = messages.stream().map(x -> BeanUtils.copyProperties(x, GroupMessageDTO.class)).collect(Collectors.toList());
        return DeveloperResult.success(list);
    }

    @Override
    public DeveloperResult insertMessage(MessageInsertDTO dto) {
        return null;
    }

    @Override
    public DeveloperResult deleteMessage(Long friendId) {
        return DeveloperResult.success();
    }

    private GroupMessagePO createGroupMessageMode(Long groupId, Long sendId, String sendNickName, List<Long> atUserIds, String message, MessageContentTypeEnum messageContentType){
        GroupMessagePO groupMessage = new GroupMessagePO();
        groupMessage.setGroupId(groupId);
        groupMessage.setSendId(sendId);
        groupMessage.setSendNickName(sendNickName);
        groupMessage.setMessageContent(message);
        groupMessage.setMessageContentType(messageContentType.code());
        groupMessage.setMessageStatus(0);
        groupMessage.setSendTime(new Date());
        if(atUserIds!=null) {
            groupMessage.setAtUserIds(atUserIds.toString());
        }
        return groupMessage;
    }
}
