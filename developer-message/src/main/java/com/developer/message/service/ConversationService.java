package com.developer.message.service;

import com.developer.framework.enums.message.MessageContentTypeEnum;
import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.ChatConversationListResponseDTO;
import com.developer.message.dto.UpsertConversationRequestDTO;

import java.util.List;

public interface ConversationService {

    /*
    获取当前用户聊天会话列表
     */
    DeveloperResult<List<ChatConversationListResponseDTO>> findChatConversationList();

    /*
    新增修改会话内容
     */
    DeveloperResult<Boolean> upsertCurrentConversation(UpsertConversationRequestDTO dto);

}
