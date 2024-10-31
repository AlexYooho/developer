package com.developer.im.listener;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.RabbitMQEventTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.framework.processor.MessageProcessorFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * rabbitmq事件监听器
 */
@Component
@Slf4j
@RabbitListener(queues = {DeveloperMQConstant.MESSAGE_IM_QUEUE})
public class RabbitMQEventListener {

    @Autowired
    private MessageProcessorFactory messageProcessorFactory;

    @RabbitHandler
    public void messageSubscribe(RabbitMQMessageBodyDTO dto, Channel channel, Message message) throws IOException {
        try {
            LocalDateTime begin = LocalDateTime.now();
            IMessageProcessor instance = messageProcessorFactory.getInstance(dto.getMessageType());
            if(instance==null){
                log.info("【IM消息服务】消息内容:{},没有对应的消息处理器",dto);
                return;
            }

            DeveloperResult<Boolean> result = instance.processor(dto);
            log.info("【IM消息服务】消息内容：{},处理时间:{},处理结果:{}",dto, Duration.between(begin, LocalDateTime.now()).getSeconds(), result);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }
    }

}
