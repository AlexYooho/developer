package com.developer.message.service.factory;

import com.developer.message.service.MessageService;
import com.developer.message.service.MessageServiceFactory;
import com.developer.message.service.impl.GroupMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMessageServiceFactory extends MessageServiceFactory {

    @Autowired
    private GroupMessageServiceImpl messageService;

    @Override
    public MessageService createMessageService() {
        return messageService;
    }
}
