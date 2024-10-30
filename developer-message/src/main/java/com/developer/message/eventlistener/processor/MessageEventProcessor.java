package com.developer.message.eventlistener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageEventProcessor implements IMessageProcessor {

    @Autowired
    private MessageServiceRegister messageServiceRegister;

    @Override
    public RabbitMQEventTypeEnum eventType() {
        return RabbitMQEventTypeEnum.MESSAGE;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        SendMessageRequestDTO messageContent = dto.parseData(SendMessageRequestDTO.class);
        if(messageContent==null){
            return DeveloperResult.error("消息体为空");
        }

        messageServiceRegister.getMessageService(messageContent.getMessageMainType()).sendMessage(messageContent);
        return DeveloperResult.success();
    }
}
