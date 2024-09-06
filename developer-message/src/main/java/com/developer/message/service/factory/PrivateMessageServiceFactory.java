package com.developer.message.service.factory;

import com.developer.message.service.MessageService;
import com.developer.message.service.MessageServiceFactory;
import com.developer.message.service.impl.PrivateMessageServiceImpl;

public class PrivateMessageServiceFactory extends MessageServiceFactory {
    @Override
    public MessageService createMessageService() {
        return new PrivateMessageServiceImpl();
    }
}