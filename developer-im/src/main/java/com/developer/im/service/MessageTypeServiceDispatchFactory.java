package com.developer.im.service;

import com.developer.framework.enums.message.MessageConversationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageTypeServiceDispatchFactory {


    @Autowired
    private ApplicationContext context;

    public AbstractMessageTypeService getInstance(MessageConversationTypeEnum messageConversationTypeEnum) {
        Map<String, AbstractMessageTypeService> beansMap = context.getBeansOfType(AbstractMessageTypeService.class);
        AbstractMessageTypeService instance = null;
        for (AbstractMessageTypeService item : beansMap.values()) {
            if (item.messageMainTypeEnum() != messageConversationTypeEnum) {
                continue;
            }

            instance = item;
            break;
        }

        return instance;
    }


}
