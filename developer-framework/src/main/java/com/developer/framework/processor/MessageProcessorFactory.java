package com.developer.framework.processor;

import com.developer.framework.enums.RabbitMQEventTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理器工厂
 */
@Component
public class MessageProcessorFactory {

    private final Map<RabbitMQEventTypeEnum, IMessageProcessor> map = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, IMessageProcessor> beans = applicationContext.getBeansOfType(IMessageProcessor.class);
        beans.values().forEach(x->map.put(x.eventType(),x));
    }

    public IMessageProcessor getInstance(RabbitMQEventTypeEnum eventType) {
        return map.get(eventType);
    }

}
