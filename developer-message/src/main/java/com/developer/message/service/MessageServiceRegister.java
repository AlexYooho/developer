package com.developer.message.service;

import com.developer.framework.enums.MessageMainTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class MessageServiceRegister {

    private final Map<MessageMainTypeEnum, MessageService> messageServiceMap = new HashMap<>();

    public MessageService getMessageService(MessageMainTypeEnum type) {
        return messageServiceMap.get(type);
    }

    public void registerMessageService(MessageMainTypeEnum type, MessageService factory) {
        messageServiceMap.put(type, factory);
    }

}
