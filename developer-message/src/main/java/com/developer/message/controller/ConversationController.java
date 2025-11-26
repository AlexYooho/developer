package com.developer.message.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.ChatConversationListResponseDTO;
import com.developer.message.service.ConversationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("conversation")
@AllArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /*
    获取会话列表
     */
    @GetMapping("list")
    public DeveloperResult<List<ChatConversationListResponseDTO>> findConversationList() {
        return conversationService.findChatConversationList();
    }
}
