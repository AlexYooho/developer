package com.developer.payment.utils;

import com.developer.framework.constant.DeveloperMQConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DelayMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendDelayMessage(Object message, int delayTime) {
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_DELAYED_EXCHANGE, DeveloperMQConstant.MESSAGE_DELAYED_ROUTING_KEY, message, processor -> {
            processor.getMessageProperties().setHeader("x-delay", delayTime);
            return processor;
        });
    }

}
