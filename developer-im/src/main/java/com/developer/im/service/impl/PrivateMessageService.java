package com.developer.im.service.impl;

import com.developer.framework.dto.MessageDTO;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
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
    public MessageMainTypeEnum messageMainTypeEnum() {
        return MessageMainTypeEnum.PRIVATE_MESSAGE;
    }

    @Override
    public DeveloperResult<Boolean> handler(MessageDTO dto) {
        PrivateMessageDTO privateMessageDTO = BeanUtils.copyProperties(dto, PrivateMessageDTO.class);
        privateMessageDTO = privateMessageDTO == null ? new PrivateMessageDTO() : privateMessageDTO;
        privateMessageDTO.setReceiverId(dto.getReceiverIds().get(0));
        privateMessageDTO.setId(dto.getMessageId());
        privateMessageDTO.setMessageContentType(dto.getMessageContentTypeEnum().code());

        IMPrivateMessageModel<PrivateMessageDTO> sendMessage = new IMPrivateMessageModel<>();
        sendMessage.setSerialNo(dto.getSerialNo());
        sendMessage.setSender(new IMUserInfoModel(dto.getSendId(), dto.getTerminalType()));
        sendMessage.setReceiverId(dto.getReceiverIds().get(0));
        sendMessage.setData(privateMessageDTO);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        return imClients.sendPrivateMessage(sendMessage, IMCmdType.PRIVATE_MESSAGE);
    }
}
