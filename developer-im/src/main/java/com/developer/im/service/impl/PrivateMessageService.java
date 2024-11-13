package com.developer.im.service.impl;

import com.developer.framework.dto.MessageDTO;
import com.developer.framework.utils.BeanUtils;
import com.developer.im.dto.PrivateMessageDTO;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMPrivateMessageModel;
import com.developer.im.model.IMUserInfoModel;
import com.developer.im.netty.IMClient;
import com.developer.im.service.AbstractMessageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageService extends AbstractMessageTypeService {

    @Autowired
    private IMClient imClients;

    @Override
    public void handler(MessageDTO dto) {
        PrivateMessageDTO privateMessageDTO = BeanUtils.copyProperties(dto, PrivateMessageDTO.class);
        privateMessageDTO.setReceiverId(dto.getReceiverIds().get(0));
        privateMessageDTO.setId(dto.getMessageId());
        privateMessageDTO.setMessageContentType(dto.getMessageContentTypeEnum().code());

        IMPrivateMessageModel<PrivateMessageDTO> sendMessage = new IMPrivateMessageModel<>();
        sendMessage.setSender(new IMUserInfoModel(dto.getSendId(),dto.getTerminalType()));
        sendMessage.setReceiverId(dto.getReceiverIds().get(0));
        sendMessage.setData(privateMessageDTO);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);

        imClients.sendPrivateMessage(sendMessage, IMCmdType.PRIVATE_MESSAGE);
    }
}
