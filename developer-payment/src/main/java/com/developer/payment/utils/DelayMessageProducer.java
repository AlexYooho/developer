package com.developer.payment.utils;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.utils.TokenUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class DelayMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JwtDecoder jwtDecoder;

    public void sendDelayMessage(Object message, int delayTime) {
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_DELAYED_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAYED_ROUTING_KEY, message, processor -> {
            processor.getMessageProperties().setHeader("x-delay", delayTime);
            return processor;
        });
    }

    public void sendMessage(String exchange, String routingKey, RabbitMQMessageBodyDTO message){
        message.token= TokenUtil.getToken();
//        Jwt decode = jwtDecoder.decode(message.token);
//        Authentication authenticationToken = new JwtAuthenticationToken(decode);
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }

}
