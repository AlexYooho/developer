package com.developer.friend.util;

import com.developer.friend.constant.DeveloperMQConstant;
import com.developer.friend.constant.MQMessageTypeConstant;
import com.developer.friend.dto.MQMessageDTO;
import com.developer.friend.dto.MessageDTO;
import com.developer.friend.enums.IMTerminalType;
import com.developer.friend.enums.MessageContentType;
import com.developer.friend.enums.MessageMainType;
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

    public void pushMQMessage(MessageMainType messageMainType, MessageContentType messageContentType, Long messageId, Long groupId, Long sendId, String sendNickName, String messageContent, List<Long> receiverIds, List<Long> atUserIds, Integer messageStatus, IMTerminalType terminalType, Date sendTime){
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMessageMainType(messageMainType);
        messageDTO.setMessageContentType(messageContentType);
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


}
