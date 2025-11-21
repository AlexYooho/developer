package com.developer.message.service.factory;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageTypeProcessorDispatchFactory {

    @Autowired
    private ApplicationContext context;


    public MessageService getInstance(MessageMainTypeEnum messageMainTypeEnum) {
        Map<String, MessageService> beansMap = context.getBeansOfType(MessageService.class);
        MessageService instance = null;
        for (MessageService item : beansMap.values()) {
            if (item.messageMainType() != messageMainTypeEnum) {
                continue;
            }

            instance = item;
            break;
        }

        return instance;
    }

}
