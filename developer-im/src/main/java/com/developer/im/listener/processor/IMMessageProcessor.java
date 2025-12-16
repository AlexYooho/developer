package com.developer.im.listener.processor;

import com.alibaba.fastjson.JSON;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.common.ProcessorTypeEnum;
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
    //@MessageRouterAspect
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        // 数据转换
        ChatMessageDTO chatMessageDTO = dto.parseData(ChatMessageDTO.class);
        chatMessageDTO.setSerialNo(dto.getSerialNo());

        // 会话类型
        MessageConversationTypeEnum messageConversationTypeEnum = getMessageConversationType(chatMessageDTO.getMessageConversationTypeEnum());

        // 获取对应的消息处理器
        AbstractMessageTypeService messageService = dispatchFactory.getInstance(messageConversationTypeEnum);
        if(messageService==null){
            log.info("【IM消息服务】消息内容:{},没有对应的消息处理器", JSON.toJSON(dto));
            return DeveloperResult.error(dto.getSerialNo(),"【IM消息服务】消息内容:"+JSON.toJSON(dto)+",没有对应的消息处理器");
        }
        return messageService.handler(chatMessageDTO);
    }

    /*
    获取会话类型
     */
    private MessageConversationTypeEnum getMessageConversationType(MessageConversationTypeEnum conversationTypeEnum){
        if(conversationTypeEnum.equals(MessageConversationTypeEnum.PRIVATE_MESSAGE)){
            return MessageConversationTypeEnum.CHAT_MESSAGE;
        }

        if(conversationTypeEnum.equals(MessageConversationTypeEnum.GROUP_MESSAGE)){
            return MessageConversationTypeEnum.CHAT_MESSAGE;
        }

        return conversationTypeEnum;
    }
}
