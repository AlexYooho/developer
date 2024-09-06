package com.developer.message.service;

import com.developer.message.service.factory.GroupMessageServiceFactory;
import com.developer.message.service.factory.PrivateMessageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageServiceFactoryRegistry {

    private final Map<MessageServiceType, MessageServiceFactory> factoryMap = new HashMap<>();

    @Autowired
    private GroupMessageServiceFactory groupMessageServiceFactory;

    @Autowired
    private PrivateMessageServiceFactory privateMessageServiceFactory;

    public MessageServiceFactoryRegistry() {
        // 注册工厂
        factoryMap.put(MessageServiceType.GROUP, groupMessageServiceFactory);
        factoryMap.put(MessageServiceType.PRIVATE, privateMessageServiceFactory);
    }

    public MessageServiceFactory getFactory(MessageServiceType type) {
        return factoryMap.get(type);
    }
}
