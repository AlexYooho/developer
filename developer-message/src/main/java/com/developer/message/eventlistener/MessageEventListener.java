package com.developer.message.eventlistener;

import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.dto.MessageBodyDTO;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.message.service.MessageServiceRegister;
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
@RabbitListener(queues = {DeveloperMQConstant.MESSAGE_CHAT_QUEUE})
public class MessageEventListener {

    @Autowired
    private MessageServiceRegister messageServiceRegister;

    @RabbitHandler
    public void messageSubscribe(MessageBodyDTO<PaymentInfoDTO> dto, Channel channel, Message message) throws IOException {
        try {
            LocalDateTime begin = LocalDateTime.now();

            //messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).sendMessage(req);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("【支付服务】消息内容：{},处理时间:{},处理结果:{}",dto, Duration.between(begin,LocalDateTime.now()).getSeconds(), JSON.toJSON(""));
        } catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            log.info("【支付服务】异常内容:{},消息内容:{}", Arrays.toString(e.getStackTrace()),dto);
        }
    }

}
