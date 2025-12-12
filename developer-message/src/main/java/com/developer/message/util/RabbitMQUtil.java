package com.developer.message.util;

import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.framework.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    /*
    发送消息
     */
    public void sendMessage(String serialNo, String exchange, String routingKey, ProcessorTypeEnum messageType, Object content) {
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(serialNo)
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(JSON.toJSON(content))
                .processorType(messageType)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
    }

    /*
    发送聊天消息
     */
    public void sendChatMessage(Object messageContent){
        RabbitMQMessageBodyDTO dto = new RabbitMQMessageBodyDTO();
        dto.setSerialNo(SerialNoHolder.getSerialNo());
        dto.setType(MQMessageTypeConstant.SENDMESSAGE);
        dto.setProcessorType(ProcessorTypeEnum.IM);
        dto.setToken(TokenUtil.getToken());
        dto.setData(JSON.toJSON(messageContent));
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_IM_EXCHANGE,DeveloperMQConstant.MESSAGE_IM_ROUTING_KEY,dto);
    }

    /*
    发送延迟消息
     */
    public void sendDelayMessage(String serialNo, String exchange, String routingKey, ProcessorTypeEnum messageType, Object content, int delayTime) {
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(serialNo)
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(JSON.toJSON(content))
                .processorType(messageType)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, dto, processor -> {
            processor.getMessageProperties().setHeader("x-delay", delayTime);
            return processor;
        });
    }

}
