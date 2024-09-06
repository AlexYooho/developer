package com.developer.message.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageService;

public class GroupMessageServiceImpl implements MessageService {
    @Override
    public DeveloperResult loadMessage(Long minId) {
        return null;
    }

    @Override
    public DeveloperResult sendMessage(SendMessageRequestDTO req) {
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
