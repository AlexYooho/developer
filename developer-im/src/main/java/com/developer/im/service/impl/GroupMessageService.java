package com.developer.im.service.impl;

import com.developer.framework.dto.MessageDTO;
import com.developer.framework.utils.BeanUtils;
import com.developer.im.dto.GroupMessageDTO;
import com.developer.im.model.IMGroupMessageModel;
import com.developer.im.model.IMUserInfoModel;
import com.developer.im.netty.IMClient;
import com.developer.im.service.AbstractMessageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageService extends AbstractMessageTypeService {

    @Autowired
    private IMClient imClients;

    @Override
    public void handler(MessageDTO dto) {
        GroupMessageDTO groupMessageDTO = BeanUtils.copyProperties(dto, GroupMessageDTO.class);
        assert groupMessageDTO != null;
        groupMessageDTO.setId(dto.getMessageId());
        groupMessageDTO.setMessageContentType(dto.getMessageContentTypeEnum().code());
        IMGroupMessageModel<GroupMessageDTO> sendMessage = new IMGroupMessageModel<>();
        sendMessage.setSender(new IMUserInfoModel(dto.getSendId(),dto.getTerminalType()));
        sendMessage.setRecvIds(dto.getReceiverIds());
        sendMessage.setData(groupMessageDTO);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClients.sendGroupMessage(sendMessage);
    }
}
