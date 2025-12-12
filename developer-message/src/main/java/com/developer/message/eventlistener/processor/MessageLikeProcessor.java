package com.developer.message.eventlistener.processor;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.enums.common.ProcessorTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.processor.IMessageProcessor;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.dto.MessageLikeEventDTO;
import com.developer.framework.enums.message.MessageLikeEnum;
import com.developer.message.pojo.GroupMessagePO;
import com.developer.message.pojo.MessageLikeRecordPO;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.repository.MessageLikeRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MessageLikeProcessor implements IMessageProcessor {

    @Autowired
    private MessageLikeRecordRepository messageLikeRecordRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Override
    public ProcessorTypeEnum processorType() {
        return ProcessorTypeEnum.MESSAGE_LIKE;
    }

    @Override
    public DeveloperResult<Boolean> processor(RabbitMQMessageBodyDTO dto) {
        MessageLikeEventDTO event = dto.parseData(MessageLikeEventDTO.class);
        Long messageId = event.getMessageId();
        Long userId = event.getUserId();

        // 再次检查数据库中是否已存在点赞记录
        MessageLikeRecordPO likeRecord = messageLikeRecordRepository.findLikeRecord(messageId, userId, MessageConversationTypeEnum.GROUP_MESSAGE);
        if (likeRecord == null) {
            // 插入新的点赞记录
            messageLikeRecordRepository.save(MessageLikeRecordPO.builder()
                    .messageId(messageId)
                    .messageType(event.getMessageConversationTypeEnum())
                    .userId(userId)
                    .LikeStatus(MessageLikeEnum.LIKE)
                    .LikeTime(new Date())
                    .CreateTime(new Date())
                    .UpdateTime(new Date())
                    .build());
        }

        if(event.getMessageConversationTypeEnum().equals(MessageConversationTypeEnum.GROUP_MESSAGE)) {
            // 更新消息的点赞数
            GroupMessagePO message = groupMessageRepository.getById(messageId);
            if (message != null) {
                message.setLikeCount(message.getLikeCount() + 1);
                groupMessageRepository.updateById(message);
            }
        }
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
