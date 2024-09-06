package com.developer.message.service.impl;

import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.client.FriendClient;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageServiceImpl implements MessageService {

    @Autowired
    private FriendClient friendClient;

    @Override
    public DeveloperResult loadMessage(Long minId) {
        return null;
    }

    @Override
    public DeveloperResult sendMessage(SendMessageRequestDTO req) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        DeveloperResult friend = friendClient.isFriend(userId, req.getReceiverId());
        return null;
    }

    @Override
    public DeveloperResult readMessage(Long friendId) {
        return null;
    }

    @Override
    public DeveloperResult recallMessage(Long id) {
        return null;
    }

    @Override
    public DeveloperResult findHistoryMessage(Long friendId, Long page, Long size) {
        return null;
    }
}
