package com.developer.payment.utils;

import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.ProcessorTypeEnum;
import com.developer.framework.utils.TokenUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param exchange
     * @param routingKey
     * @param content
     */
    public void sendMessage(String serialNo,String exchange, String routingKey, ProcessorTypeEnum messageType, Object content){
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(serialNo)
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(JSON.toJSON(content))
                .processorType(messageType)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey,dto);
    }

    /**
     * 发送延迟消息
     * @param exchange
     * @param routingKey
     * @param content
     * @param delayTime 单位s
     */
    public void sendDelayMessage(String serialNo,String exchange, String routingKey, ProcessorTypeEnum messageType, Object content, Integer delayTime) {
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(serialNo)
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(JSON.toJSON(content))
                .processorType(messageType)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, dto, processor -> {
            processor.getMessageProperties().setHeader("x-delay", delayTime*1000);
            return processor;
        });
    }

}
