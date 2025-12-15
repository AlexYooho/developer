package com.developer.im.service.impl;

import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMChatMessageBaseModel;
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
    public MessageConversationTypeEnum messageMainTypeEnum() {
        return MessageConversationTypeEnum.PRIVATE_MESSAGE;
    }

    @Override
    public DeveloperResult<Boolean> handler(ChatMessageDTO dto) {

        // 只是组装了个对象
        IMChatMessageBaseModel model = new IMChatMessageBaseModel();
        model.setSerialNo(dto.getSerialNo());
        model.setMessageId(dto.getMessageId());
        model.setMessageContent(dto.getMessageContent());
        model.setMessageContentType(dto.getMessageContentTypeEnum());
        model.setMessageStatus(dto.getMessageStatus());
        //model.setReceiverId(dto.getFriendUserId());
        model.setSendToSelf(true);
        model.setSender(new IMUserInfoModel(dto.getSendId(),dto.getTerminalType(),dto.getSendNickName()));
        model.setSendId(dto.getSendId());
        model.setSendTime(dto.getSendTime());
        model.setSendResult(false);
        return imClients.sendPrivateMessage(model, IMCmdType.PRIVATE_MESSAGE);
    }
}
