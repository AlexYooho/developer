package com.developer.message.util;

import com.developer.framework.constant.DeveloperMQConstant;
import com.developer.framework.constant.MQMessageTypeConstant;
import com.developer.framework.dto.MQMessageDTO;
import com.developer.framework.dto.MessageDTO;
import com.developer.framework.enums.IMTerminalTypeEnum;
import com.developer.framework.enums.MessageContentTypeEnum;
import com.developer.framework.enums.MessageMainTypeEnum;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void pushMQMessage(MessageMainTypeEnum messageMainTypeEnum, MessageContentTypeEnum messageContentTypeEnum, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, Integer messageStatus, IMTerminalTypeEnum terminalType, Date sendTime){
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMessageMainTypeEnum(messageMainTypeEnum);
        messageDTO.setMessageContentTypeEnum(messageContentTypeEnum);
        messageDTO.setMessageId(messageId);
        messageDTO.setGroupId(groupId);
        messageDTO.setSendId(sendId);
        messageDTO.setSendNickName(sendNickName);
        messageDTO.setMessageContent(messageContent);
        messageDTO.setReceiverIds(receiverIds);
        messageDTO.setAtUserIds(atUserIds);
        messageDTO.setMessageStatus(messageStatus);
        messageDTO.setTerminalType(terminalType);
        messageDTO.setSendTime(sendTime);

        MQMessageDTO<MessageDTO> data = new MQMessageDTO<>();
        data.setSerialNo(UUID.randomUUID().toString());
        data.setType(MQMessageTypeConstant.SENDMESSAGE);
        data.setData(messageDTO);
        rabbitTemplate.convertAndSend(DeveloperMQConstant.MESSAGE_EXCHANGE,DeveloperMQConstant.ROUTING_KEY,data);
    }

    public void pushMessage(String queue, Object data){
        rabbitTemplate.convertAndSend(queue,data);
    }
}
