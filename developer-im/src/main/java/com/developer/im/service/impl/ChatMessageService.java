package com.developer.im.service.impl;

import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.dto.ChatMessageBodyDTO;
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
    public MessageConversationTypeEnum messageMainTypeEnum() {
        return MessageConversationTypeEnum.CHAT_MESSAGE;
    }

    /**
     * 这里需要切面判断，需要推送消息的用户客户端和当前服务端是否存在映射关系
     *
     * @param chatMessageDTO
     * @return
     */
    @Override
    public DeveloperResult<Boolean> handler(ChatMessageDTO chatMessageDTO) {

        // 统一处理聊天参数，需要传递给前端的
        ChatMessageBodyDTO message = new ChatMessageBodyDTO();
        message.setTargetIds(chatMessageDTO.getTargetIds());
        // 私聊还是群聊,根据会话类型来判断
        message.setCmd(chatMessageDTO.getMessageConversationTypeEnum().equals(MessageConversationTypeEnum.PRIVATE_MESSAGE) ? IMCmdType.PRIVATE_MESSAGE : IMCmdType.GROUP_MESSAGE);

        ChatMessageBodyDTO.ChatMessageBodyItemDTO itemDTO = new ChatMessageBodyDTO.ChatMessageBodyItemDTO();
        itemDTO.setSerialNo(chatMessageDTO.getSerialNo());
        itemDTO.setSender(new IMUserInfoModel(chatMessageDTO.getSendId(), chatMessageDTO.getTerminalType(), chatMessageDTO.getSendNickName()));
        itemDTO.setMessageConversationTypeEnum(chatMessageDTO.getMessageConversationTypeEnum());
        itemDTO.setMessageContentTypeEnum(chatMessageDTO.getMessageContentTypeEnum());
        itemDTO.setMessageId(chatMessageDTO.getMessageId());
        itemDTO.setMessageContent(chatMessageDTO.getMessageContent());
        itemDTO.setSendTime(chatMessageDTO.getSendTime());
        itemDTO.setAtUserIds(chatMessageDTO.getAtUserIds());
        itemDTO.setGroupId(chatMessageDTO.getGroupId());

        message.setBodyItem(itemDTO);

        return imClient.sendChatMessage(message);
    }
}
