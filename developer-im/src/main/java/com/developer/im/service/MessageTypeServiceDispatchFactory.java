package com.developer.im.service;

import com.developer.framework.enums.MessageMainTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageTypeServiceDispatchFactory {


    @Autowired
    private ApplicationContext context;

    public AbstractMessageTypeService getInstance(MessageMainTypeEnum messageMainTypeEnum) {
        Map<String, AbstractMessageTypeService> beansMap = context.getBeansOfType(AbstractMessageTypeService.class);
        AbstractMessageTypeService instance = null;
        for (AbstractMessageTypeService item : beansMap.values()) {
            if (item.messageMainTypeEnum() != messageMainTypeEnum) {
                continue;
            }

            instance = item;
            break;
        }

        return instance;
    }


}
