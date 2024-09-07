package com.developer.message.service;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.service.impl.GroupMessageServiceImpl;
import com.developer.message.service.impl.PrivateMessageServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageServiceConfig {

    @Bean
    public MessageServiceRegister messageServiceFactoryRegistry(
            GroupMessageServiceImpl groupMessageService,
            PrivateMessageServiceImpl privateMessageService) {

        MessageServiceRegister registry = new MessageServiceRegister();
        registry.registerMessageService(MessageMainTypeEnum.GROUP_MESSAGE, groupMessageService);
        registry.registerMessageService(MessageMainTypeEnum.PRIVATE_MESSAGE, privateMessageService);

        return registry;
    }

}
