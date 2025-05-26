package com.developer.im.service;

import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;

public abstract class AbstractMessageTypeService {

    public abstract MessageMainTypeEnum messageMainTypeEnum();

    public abstract DeveloperResult<Boolean> handler(ChatMessageDTO chatMessageDTO);

}
