package com.developer.message.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.ChatConversationListResponseDTO;

import java.util.List;

public interface ConversationService {

    /*
    获取当前用户聊天会话列表
     */
    DeveloperResult<List<ChatConversationListResponseDTO>> findChatConversationList();

}
