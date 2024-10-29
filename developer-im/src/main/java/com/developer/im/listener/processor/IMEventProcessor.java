package com.developer.im.listener.processor;

import com.developer.framework.dto.MessageDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.im.service.AbstractMessageTypeService;
import com.developer.im.service.MessageServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IMEventProcessor implements IMessageProcessor {

    @Autowired
    private MessageServiceRegister messageServiceRegister;

    @Override
    public RabbitMQEventTypeEnum eventType() {
        return RabbitMQEventTypeEnum.IM;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        MessageDTO messageDTO = dto.parseData(MessageDTO.class);
        AbstractMessageTypeService messageService = messageServiceRegister.getMessageService(messageDTO.getMessageMainTypeEnum());
        if(messageService==null){
            log.info("【IM消息服务】消息内容:{},没有对应的消息处理器",dto);
            return DeveloperResult.error();
        }
        messageService.handler(messageDTO);
        return DeveloperResult.success();
    }
}
