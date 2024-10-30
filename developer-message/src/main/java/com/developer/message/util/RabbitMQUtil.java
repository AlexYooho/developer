package com.developer.message.util;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
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
    public void sendMessage(String exchange,String routingKey,Object content){
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(content)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey,dto);
    }

    /**
     * 发送延迟消息
     * @param exchange
     * @param routingKey
     * @param content
     * @param delayTime
     */
    public void sendDelayMessage(String exchange,String routingKey,Object content, int delayTime) {
        RabbitMQMessageBodyDTO dto = RabbitMQMessageBodyDTO.builder()
                .serialNo(UUID.randomUUID().toString())
                .type(MQMessageTypeConstant.SENDMESSAGE)
                .token(TokenUtil.getToken())
                .data(content)
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, dto, processor -> {
            processor.getMessageProperties().setHeader("x-delay", delayTime);
            return processor;
        });
    }

}
