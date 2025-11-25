package com.developer.message.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.ChatConversationListResponseDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final MessageConversationRepository messageConversationRepository;
    private final RedisUtil redisUtil;
    private final RpcClient rpcClient;

    @Override
    public DeveloperResult<List<ChatConversationListResponseDTO>> findChatConversationList() {
        List<ChatConversationListResponseDTO> list = new ArrayList<>();

        // 先去缓存获取
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        String key = RedisKeyConstant.CONVERSATION_LIST_KEY(userId);
        list = redisUtil.get(key, new TypeReference<List<ChatConversationListResponseDTO>>() {
        });
        if (list != null) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
        }

        // 没有则去查库
        list = new ArrayList<>();
        List<MessageConversationPO> conversationPOS = messageConversationRepository.findList(userId);

        if (conversationPOS == null || conversationPOS.isEmpty()) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
        }

        // 获取好友信息
        DeveloperResult<List<FriendInfoResponseRpcDTO>> friendResult = RpcExecutor
                .execute(() -> rpcClient.friendRpcService.findFriends());
        Map<Long, FriendInfoResponseRpcDTO> friendMap = new HashMap<>();
        if (friendResult.getData() != null) {
            friendMap = friendResult.getData().stream()
                    .collect(Collectors.toMap(FriendInfoResponseRpcDTO::getId, Function.identity()));
        }

        // 获取群组信息
        DeveloperResult<List<GroupInfoResponseRpcDTO>> groupResult = RpcExecutor
                .execute(() -> rpcClient.groupRpcService.getSelfJoinAllGroupInfo(SerialNoHolder.getSerialNo()));
        Map<Long, GroupInfoResponseRpcDTO> groupMap = new HashMap<>();
        if (groupResult.getData() != null) {
            groupMap = groupResult.getData().stream()
                    .collect(Collectors.toMap(GroupInfoResponseRpcDTO::getGroupId, Function.identity()));
        }

        for (MessageConversationPO po : conversationPOS) {
            ChatConversationListResponseDTO dto = new ChatConversationListResponseDTO();
            BeanUtils.copyProperties(po, dto);

            if (po.getConvType() == 0) {
                // 私聊
                FriendInfoResponseRpcDTO friendInfoDTO = friendMap.get(po.getTargetId());
                if (friendInfoDTO != null) {
                    dto.setName(friendInfoDTO.getNickName());
                    dto.setHeadImage(friendInfoDTO.getHeadImage());
                }
            } else if (po.getConvType() == 1) {
                // 群聊
                GroupInfoResponseRpcDTO groupInfoDTO = groupMap.get(po.getTargetId());
                if (groupInfoDTO != null) {
                    dto.setName(groupInfoDTO.getGroupName());
                    dto.setHeadImage(groupInfoDTO.getGroupHeadImage());
                }
            }

            list.add(dto);
        }

        // 再次存入缓存--30~60秒过期
        redisUtil.set(key, list, 60, TimeUnit.SECONDS);

        return DeveloperResult.success(SerialNoHolder.getSerialNo(), list);
    }
}
