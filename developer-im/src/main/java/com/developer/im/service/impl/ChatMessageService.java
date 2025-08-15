package com.developer.im.service.impl;

import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.dto.PushMessageBodyDTO;
import com.developer.im.dto.PushMessageBodyDataDTO;
import com.developer.im.enums.IMCmdType;
import com.developer.im.model.IMUserInfoModel;
import com.developer.im.netty.IMClient;
import com.developer.im.service.AbstractMessageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService extends AbstractMessageTypeService {

    @Autowired
    private IMClient imClient;

    @Override
    public MessageMainTypeEnum messageMainTypeEnum() {
        return MessageMainTypeEnum.CHAT_MESSAGE;
    }

    /**
     * 这里需要切面判断，需要推送消息的用户客户端和当前服务端是否存在映射关系
     * @param chatMessageDTO
     * @return
     */
    @Override
    public DeveloperResult<Boolean> handler(ChatMessageDTO chatMessageDTO) {
        List<Long> receiverIds = Optional.ofNullable(chatMessageDTO.getReceiverIds()).orElse(new ArrayList<>());
        receiverIds.add(chatMessageDTO.getFriendUserId());

        PushMessageBodyDataDTO dataDTO = new PushMessageBodyDataDTO();
        dataDTO.setSerialNo(chatMessageDTO.getSerialNo());
        dataDTO.setMessageId(chatMessageDTO.getMessageId());
        dataDTO.setMessageContent(chatMessageDTO.getMessageContent());
        dataDTO.setMessageContentType(chatMessageDTO.getMessageContentTypeEnum());
        dataDTO.setMessageStatus(chatMessageDTO.getMessageStatus());
        dataDTO.setSendToSelf(true);
        dataDTO.setSender(new IMUserInfoModel(chatMessageDTO.getSendId(), chatMessageDTO.getTerminalType(),chatMessageDTO.getSendNickName()));
        dataDTO.setSendId(chatMessageDTO.getSendId());
        dataDTO.setSendTime(chatMessageDTO.getSendTime());
        dataDTO.setSendResult(false);
        dataDTO.setGroupId(chatMessageDTO.getGroupId());
        dataDTO.setReceiverIds(receiverIds);
        dataDTO.setAtUserIds(chatMessageDTO.getAtUserIds());

        // 这里构建推送消息模型
        PushMessageBodyDTO message = new PushMessageBodyDTO();
        message.setSerialNo(chatMessageDTO.getSerialNo());
        message.setCmd(IMCmdType.PRIVATE_MESSAGE);
        message.setSender(new IMUserInfoModel());
        message.setMessageReceiverIds(receiverIds);
        message.setData(dataDTO);

        return imClient.pushMessage(message);
    }
}
