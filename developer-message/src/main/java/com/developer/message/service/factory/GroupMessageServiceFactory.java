package com.developer.message.service.factory;

import com.developer.message.service.MessageService;
import com.developer.message.service.MessageServiceFactory;
import com.developer.message.service.impl.GroupMessageServiceImpl;

public class GroupMessageServiceFactory extends MessageServiceFactory {
    @Override
    public MessageService createMessageService() {
        return new GroupMessageServiceImpl();
    }
}
