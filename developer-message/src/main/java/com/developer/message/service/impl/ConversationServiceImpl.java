package com.developer.message.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.ChatConversationListResponseDTO;
import com.developer.message.repository.MessageConversationRepository;
import com.developer.message.service.ConversationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final MessageConversationRepository messageConversationRepository;

    @Override
    public DeveloperResult<List<ChatConversationListResponseDTO>> findChatConversationList() {
        List<ChatConversationListResponseDTO> list = new ArrayList<>();

        // 先去缓存获取


        // 没有则去查库


        // 再次存入缓存--30~60秒过期

        return DeveloperResult.success(SerialNoHolder.getSerialNo(),list);
    }
}
