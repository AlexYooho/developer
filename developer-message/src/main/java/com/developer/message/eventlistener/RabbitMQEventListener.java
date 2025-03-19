package com.developer.message.eventlistener;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.exception.RemoteInvokeException;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.framework.processor.ProcessorDispatchFactory;
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
@RabbitListener(queues = {DeveloperMQConstant.MESSAGE_CHAT_QUEUE})
public class RabbitMQEventListener {

    @Autowired
    private ProcessorDispatchFactory processorDispatchFactory;

    @RabbitHandler
    public void messageSubscribe(RabbitMQMessageBodyDTO dto, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            LocalDateTime begin = LocalDateTime.now();
            //IMessageProcessor instance = messageProcessorFactory.getInstance(dto.getProcessorType());
            IMessageProcessor instance = processorDispatchFactory.getInstance(dto.getProcessorType());
            if(instance==null){
                log.info("【Message服务】消息内容:{},没有对应的消息处理器",dto);
                channel.basicAck(tag,false);
                return;
            }

            DeveloperResult<Boolean> result = instance.processor(dto);
            log.info("【message服务】消息内容：{},处理时间:{}s,处理结果:{}",dto, Duration.between(begin, LocalDateTime.now()).getSeconds(), result);
            channel.basicAck(tag,false);
        } catch (Exception e){
            if(e instanceof RemoteInvokeException){
                log.error("【message服务】业务处理失败错误:{}", e.getMessage());
                channel.basicAck(tag,false);
            }else {
                log.error("【message服务】系统异常:{}", e.getMessage());
                channel.basicNack(tag, false, true);
            }
        }
    }

}
