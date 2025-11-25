package com.developer.message.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.ChatConversationListResponseDTO;
import com.developer.message.service.ConversationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("list")
    public DeveloperResult<List<ChatConversationListResponseDTO>> getConversationList() {
        return conversationService.findChatConversationList();
    }
}
