package com.developer.message.eventlistener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageTypeProcessorDispatchFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageSendProcessor implements IMessageProcessor {

    @Autowired
    private MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.MESSAGE;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        SendMessageRequestDTO messageContent = dto.parseData(SendMessageRequestDTO.class);
        if(messageContent==null){
            return DeveloperResult.error("消息体为空");
        }

        messageTypeProcessorDispatchFactory.getInstance(messageContent.getMessageMainType()).sendMessage(messageContent);

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
