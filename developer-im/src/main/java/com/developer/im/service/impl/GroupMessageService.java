package com.developer.im.service.impl;

import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.BeanUtils;
import com.developer.im.dto.GroupMessageDTO;
import com.developer.im.dto.PrivateMessageDTO;
import com.developer.im.model.IMChatMessageBaseModel;
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
    public MessageMainTypeEnum messageMainTypeEnum() {
        return MessageMainTypeEnum.GROUP_MESSAGE;
    }

    @Override
    public DeveloperResult<Boolean> handler(ChatMessageDTO dto) {
        IMChatMessageBaseModel model = new IMChatMessageBaseModel();
        model.setSerialNo(dto.getSerialNo());
        model.setMessageId(dto.getMessageId());
        model.setMessageContent(dto.getMessageContent());
        model.setMessageContentType(dto.getMessageContentTypeEnum());
        model.setMessageStatus(dto.getMessageStatus());
        model.setSendToSelf(true);
        model.setSender(new IMUserInfoModel(dto.getSendId(),dto.getTerminalType(),dto.getSendNickName()));
        model.setSendTime(dto.getSendTime());
        model.setSendResult(false);
        model.setGroupId(dto.getGroupId());
        model.setAtUserIds(dto.getAtUserIds());
        model.setReceiverIds(dto.getReceiverIds());

        return imClients.sendGroupMessage(model);
    }
}
