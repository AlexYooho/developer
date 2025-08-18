package com.developer.im.listener.processor;

import com.alibaba.fastjson.JSON;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.im.annotations.MessageRouterAspect;
import com.developer.im.service.AbstractMessageTypeService;
import com.developer.im.service.MessageTypeServiceDispatchFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IMMessageProcessor implements IMessageProcessor {

    @Autowired
    private MessageTypeServiceDispatchFactory dispatchFactory;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.IM;
    }

    @Override
    @MessageRouterAspect
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        ChatMessageDTO chatMessageDTO = dto.parseData(ChatMessageDTO.class);
        chatMessageDTO.setSerialNo(dto.getSerialNo());

        MessageMainTypeEnum messageMainTypeEnum = chatMessageDTO.getMessageMainTypeEnum().equals(MessageMainTypeEnum.PRIVATE_MESSAGE) ||
                chatMessageDTO.getMessageMainTypeEnum().equals(MessageMainTypeEnum.GROUP_MESSAGE) ? MessageMainTypeEnum.CHAT_MESSAGE : chatMessageDTO.getMessageMainTypeEnum();

        AbstractMessageTypeService messageService = dispatchFactory.getInstance(messageMainTypeEnum);
        if(messageService==null){
            log.info("【IM消息服务】消息内容:{},没有对应的消息处理器", JSON.toJSON(dto));
            return DeveloperResult.error(dto.getSerialNo(),"【IM消息服务】消息内容:"+JSON.toJSON(dto)+",没有对应的消息处理器");
        }
        return messageService.handler(chatMessageDTO);
    }
}
