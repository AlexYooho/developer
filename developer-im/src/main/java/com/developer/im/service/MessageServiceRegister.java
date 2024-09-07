package com.developer.im.service;

import com.developer.framework.enums.MessageMainTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class MessageServiceRegister {

    private final Map<MessageMainTypeEnum, AbstractMessageTypeService> messageServiceMap = new HashMap<>();

    public AbstractMessageTypeService getMessageService(MessageMainTypeEnum type) {
        return messageServiceMap.get(type);
    }

    public void registerMessageService(MessageMainTypeEnum type, AbstractMessageTypeService factory) {
        messageServiceMap.put(type, factory);
    }

}
