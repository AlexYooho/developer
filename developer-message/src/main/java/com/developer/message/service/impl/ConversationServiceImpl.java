package com.developer.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.ChatConversationListResponseDTO;
import com.developer.message.dto.UpsertConversationRequestDTO;
import com.developer.message.pojo.MessageConversationPO;
import com.developer.message.repository.MessageConversationRepository;
import com.developer.message.service.ConversationService;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.friend.response.FriendInfoResponseRpcDTO;
import com.developer.rpc.dto.group.response.GroupInfoResponseRpcDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final MessageConversationRepository messageConversationRepository;
    private final RedisUtil redisUtil;
    private final RpcClient rpcClient;

    /*
    获取当前用户会话列表
     */
    @Override
    public DeveloperResult<List<ChatConversationListResponseDTO>> findChatConversationList() {
        List<ChatConversationListResponseDTO> list = new ArrayList<>();

        // 先去缓存获取
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String key = RedisKeyConstant.CONVERSATION_LIST_KEY(userId);
        list = redisUtil.get(key, new TypeReference<List<ChatConversationListResponseDTO>>() {
        });
        if (CollUtil.isNotEmpty(list)) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
        }

        // 没有则去查库
        list = new ArrayList<>();
        List<MessageConversationPO> conversationPOS = messageConversationRepository.findList(userId);

        if (ObjectUtil.isEmpty(conversationPOS)) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
        }

        // 获取好友信息
        DeveloperResult<List<FriendInfoResponseRpcDTO>> friendResult = RpcExecutor.execute(() -> rpcClient.friendRpcService.findFriends());
        Map<Long, FriendInfoResponseRpcDTO> friendMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(friendResult.getData())) {
            friendMap = friendResult.getData().stream().collect(Collectors.toMap(FriendInfoResponseRpcDTO::getId, Function.identity()));
        }

        // 获取群组信息
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupResult = RpcExecutor.execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo());
        Map<Long, GroupInfoResponseRpcDTO> groupMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(groupResult.getData())) {
            groupMap = groupResult.getData().stream().collect(Collectors.toMap(GroupInfoResponseRpcDTO::getGroupId, Function.identity()));
        }

        for (MessageConversationPO po : conversationPOS) {
            ChatConversationListResponseDTO dto = new ChatConversationListResponseDTO();
            BeanUtils.copyProperties(po, dto);

            if (po.getConvType() == MessageConversationTypeEnum.PRIVATE_MESSAGE) {
                // 私聊
                FriendInfoResponseRpcDTO friendInfoDTO = friendMap.get(po.getTargetId());
                if (ObjectUtil.isNotEmpty(friendInfoDTO)) {
                    dto.setName(friendInfoDTO.getNickName());
                    dto.setHeadImage(friendInfoDTO.getHeadImage());
                }
            } else if (po.getConvType() == MessageConversationTypeEnum.GROUP_MESSAGE) {
                // 群聊
                GroupInfoResponseRpcDTO groupInfoDTO = groupMap.get(po.getTargetId());
                if (ObjectUtil.isNotEmpty(groupInfoDTO)) {
                    dto.setName(groupInfoDTO.getGroupName());
                    dto.setHeadImage(groupInfoDTO.getGroupAvatar());
                }
            }

            list.add(dto);
        }

        // 再次存入缓存--30~60秒过期
        redisUtil.set(key, list, ThreadLocalRandom.current().nextLong(30, 61), TimeUnit.SECONDS);

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }

    /*
    修改或新增当前用户的当前会话信息
     */
    @Override
    public DeveloperResult<Boolean> upsertCurrentConversation(UpsertConversationRequestDTO dto) {
        // 获取当前会话记录
        List<MessageConversationPO> conversations = messageConversationRepository.findList(SelfUserInfoContext.selfUserInfo().getUserId());

        // 判断需新增或修改的会话是否存在
        MessageConversationPO conversation = conversations.stream().filter(x -> x.getTargetId().equals(dto.getTargetId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(conversation)) {
            conversation = new MessageConversationPO();
            conversation.setUserId(SelfUserInfoContext.selfUserInfo().getUserId());
            conversation.setConvType(dto.getConvType());
            conversation.setTargetId(dto.getTargetId());
            conversation.setLastMsgSeq(0L);
            conversation.setLastMsgId(0L);
            conversation.setLastMsgContent(dto.getLastMsgContent());
            conversation.setLastMsgType(dto.getLastMsgType());
            conversation.setLastMsgTime(new Date());
            conversation.setUnreadCount(0);
            conversation.setPinned(false);
            conversation.setMuted(false);
            conversation.setDeleted(false);
            conversation.setDraft("");
            conversation.setCreateTime(new Date());
            conversation.setUpdateTime(new Date());

            // 新增会话
            messageConversationRepository.save(conversation);
        } else {
            // 修改会话
            assert conversation != null;
            conversation.setLastMsgSeq(dto.getLastMsgSeq() == null ? conversation.getLastMsgSeq() : dto.getLastMsgSeq());
            conversation.setLastMsgId(dto.getLastMsgId() == null ? conversation.getLastMsgId() : dto.getLastMsgId());
            conversation.setLastMsgContent(dto.getLastMsgContent() == null ? conversation.getLastMsgContent() : dto.getLastMsgContent());
            conversation.setLastMsgType(dto.getLastMsgType() == null ? conversation.getLastMsgType() : dto.getLastMsgType());
            conversation.setLastMsgTime(new Date());
            conversation.setUnreadCount(dto.getUnreadCount() == null ? conversation.getUnreadCount() : dto.getUnreadCount());
            conversation.setPinned(dto.getPinned() == null ? conversation.getPinned() : dto.getPinned());
            conversation.setMuted(dto.getMuted() == null ? conversation.getMuted() : dto.getMuted());
            conversation.setMuted(dto.getDeleted() == null ? conversation.getDeleted() : dto.getDeleted());
            conversation.setDraft(dto.getDraft() == null ? conversation.getDraft() : dto.getDraft());
            conversation.setUpdateTime(new Date());

            messageConversationRepository.updateConversationInfo(conversation);
        }

        // 更新缓存
        String key = RedisKeyConstant.CONVERSATION_LIST_KEY(SelfUserInfoContext.selfUserInfo().getUserId());
        redisUtil.delete(key);
        findChatConversationList();

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
