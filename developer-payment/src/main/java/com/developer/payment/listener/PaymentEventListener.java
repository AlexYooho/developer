package com.developer.payment.listener;


import com.alibaba.fastjson.JSON;
import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.PaymentInfoDTO;
import com.developer.payment.service.PaymentService;
import com.developer.payment.service.register.PaymentTypeRegister;
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
@RabbitListener(queues = {DeveloperMQConstant.MESSAGE_PAYMENT_QUEUE})
public class PaymentEventListener {

    @Autowired
    private PaymentTypeRegister paymentTypeRegister;

    @RabbitHandler
    public void messageSubscribe(PaymentInfoDTO dto, Channel channel, Message message) throws IOException {
        try {
            LocalDateTime begin = LocalDateTime.now();
            PaymentService paymentTypeInstance = paymentTypeRegister.findPaymentTypeInstance(dto.getPaymentTypeEnum());
            if(paymentTypeInstance==null){
                log.info("【支付服务】消息内容:{},没有对应的消息处理器",dto);
                return;
            }

            DeveloperResult<Boolean> result = paymentTypeInstance.doPay(dto);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("【支付服务】消息内容：{},处理时间:{},处理结果:{}",dto, Duration.between(begin,LocalDateTime.now()).getSeconds(), JSON.toJSON(result));
        } catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            log.info("【支付服务】异常内容:{},消息内容:{}", Arrays.toString(e.getStackTrace()),dto);
        }
    }
}
