package com.developer.im.listener;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.dto.MQMessageDTO;
import com.developer.framework.dto.MessageDTO;
import com.developer.im.messageservice.AbstractMessageTypeService;
import com.developer.im.messageservice.MessageTypeFactory;
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
import java.util.Arrays;

@Component
@Slf4j
@RabbitListener(queues = {DeveloperMQConstant.MESSAGE_QUEUE})
public class MessageListener {

    @Autowired
    private MessageTypeFactory messageTypeFactory;

    @RabbitHandler
    public void messageSubscribe(MQMessageDTO dto, Channel channel, Message message) throws IOException {
        try {
            LocalDateTime begin = LocalDateTime.now();
            MessageDTO messageDTO = (MessageDTO) dto.getData();

            AbstractMessageTypeService processor = messageTypeFactory.getMessageProcessor(messageDTO.getMessageMainTypeEnum());
            if(processor==null){
                log.info("【IM消息服务】消息内容:{},没有对应的消息处理器",dto);
                return;
            }

            processor.handler(messageDTO);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("【IM消息服务】消息内容：{},处理时间:{}",dto, Duration.between(begin,LocalDateTime.now()).getSeconds());
        } catch (Exception e){
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            log.info("【IM消息服务】异常内容:{},消息内容:{}", Arrays.toString(e.getStackTrace()),dto);
        }
    }

}
