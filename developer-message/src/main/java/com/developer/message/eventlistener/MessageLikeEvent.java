package com.developer.message.eventlistener;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.dto.MessageLikeEventDTO;
import com.developer.message.enums.MessageLikeEnum;
import com.developer.message.pojo.GroupMessagePO;
import com.developer.message.pojo.MessageLikeRecordPO;
import com.developer.message.repository.GroupMessageRepository;
import com.developer.message.repository.MessageLikeRecordRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class MessageLikeEvent {

    @Autowired
    private MessageLikeRecordRepository messageLikeRecordRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @RabbitListener(queues = "like.queue")
    @Transactional
    public void handleLikeEvent(MessageLikeEventDTO event) {
        Long messageId = event.getMessageId();
        Long userId = event.getUserId();

        // 再次检查数据库中是否已存在点赞记录
        MessageLikeRecordPO likeRecord = messageLikeRecordRepository.findLikeRecord(messageId, userId, MessageMainTypeEnum.GROUP_MESSAGE);
        if (likeRecord == null) {
            // 插入新的点赞记录
            messageLikeRecordRepository.save(MessageLikeRecordPO.builder()
                    .messageId(messageId)
                    .messageType(MessageMainTypeEnum.GROUP_MESSAGE)
                    .userId(userId)
                    .LikeStatus(MessageLikeEnum.LIKE)
                    .LikeTime(new Date())
                    .CreateTime(new Date())
                    .UpdateTime(new Date())
                    .build());
        }

        // 更新消息的点赞数
        GroupMessagePO message = groupMessageRepository.getById(messageId);
        if (message != null) {
            message.setLikeCount(message.getLikeCount() + 1);
            groupMessageRepository.updateById(message);
        }
    }

}
