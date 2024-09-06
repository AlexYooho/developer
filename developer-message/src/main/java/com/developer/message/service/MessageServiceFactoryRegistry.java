package com.developer.message.service;

import com.developer.message.service.factory.GroupMessageServiceFactory;
import com.developer.message.service.factory.PrivateMessageServiceFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageServiceFactoryRegistry {

    private final Map<MessageServiceType, MessageServiceFactory> factoryMap = new HashMap<>();

    public MessageServiceFactoryRegistry() {
        // 注册工厂
        factoryMap.put(MessageServiceType.GROUP, new GroupMessageServiceFactory());
        factoryMap.put(MessageServiceType.PRIVATE, new PrivateMessageServiceFactory());
    }

    public MessageServiceFactory getFactory(MessageServiceType type) {
        return factoryMap.get(type);
    }
}
