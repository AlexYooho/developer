package com.developer.message.service.factory;

import com.developer.message.service.MessageService;
import com.developer.message.service.MessageServiceFactory;
import com.developer.message.service.impl.PrivateMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrivateMessageServiceFactory extends MessageServiceFactory {

    @Autowired
    private PrivateMessageServiceImpl privateMessageService;

    @Override
    public MessageService createMessageService() {
        return privateMessageService;
    }
}