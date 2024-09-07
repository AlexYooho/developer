package com.developer.im.service;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.im.service.impl.GroupMessageService;
import com.developer.im.service.impl.PrivateMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageServiceConfig {

    @Bean
    public MessageServiceRegister messageServiceFactoryRegistry(
            GroupMessageService groupMessageService,
            PrivateMessageService privateMessageService) {

        MessageServiceRegister registry = new MessageServiceRegister();
        registry.registerMessageService(MessageMainTypeEnum.GROUP_MESSAGE, groupMessageService);
        registry.registerMessageService(MessageMainTypeEnum.PRIVATE_MESSAGE, privateMessageService);

        return registry;
    }

}
