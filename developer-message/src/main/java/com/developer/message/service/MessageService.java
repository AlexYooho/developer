package com.developer.message.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.SendMessageRequestDTO;

public interface MessageService {

    DeveloperResult loadMessage(Long minId);

    DeveloperResult sendMessage(SendMessageRequestDTO req);

    DeveloperResult readMessage(Long friendId);

    DeveloperResult recallMessage(Long id);

    DeveloperResult findHistoryMessage(Long friendId,Long page,Long size);

    DeveloperResult insertMessage(MessageInsertDTO dto);

    DeveloperResult deleteMessage(Long friendId);

}
