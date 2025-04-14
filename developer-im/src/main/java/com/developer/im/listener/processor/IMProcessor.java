package com.developer.im.listener.processor;

import com.alibaba.fastjson.JSON;
import com.developer.framework.dto.MessageDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.im.service.AbstractMessageTypeService;
import com.developer.im.service.MessageTypeServiceDispatchFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IMProcessor implements IMessageProcessor {

    @Autowired
    private MessageTypeServiceDispatchFactory dispatchFactory;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.IM;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        MessageDTO messageDTO = dto.parseData(MessageDTO.class);
        messageDTO.setSerialNo(dto.getSerialNo());
        AbstractMessageTypeService messageService = dispatchFactory.getInstance(messageDTO.getMessageMainTypeEnum());
        if(messageService==null){
            log.info("【IM消息服务】消息内容:{},没有对应的消息处理器", JSON.toJSON(dto));
            return DeveloperResult.error(dto.getSerialNo(),"【IM消息服务】消息内容:"+JSON.toJSON(dto)+",没有对应的消息处理器");
        }
        return messageService.handler(messageDTO);
    }
}
