package com.developer.message.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.SendMessageRequestDTO;

import java.util.List;

public interface MessageService {

    /**
     * 加载消息
     * @param minId
     * @return
     */
    DeveloperResult loadMessage(Long minId);

    /**
     * 发送消息
     * @param req
     * @return
     */
    DeveloperResult sendMessage(SendMessageRequestDTO req);

    /**
     * 已读消息
     * @param friendId
     * @return
     */
    DeveloperResult readMessage(Long friendId);

    /**
     * 撤回消息
     * @param id
     * @return
     */
    DeveloperResult<Boolean> recallMessage(Long id);

    /**
     * 查询历史记录
     * @param friendId
     * @param page
     * @param size
     * @return
     */
    DeveloperResult findHistoryMessage(Long friendId,Long page,Long size);

    /**
     * 新增消息
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto);

    /**
     * 删除消息
     * @param friendId
     * @return
     */
    DeveloperResult<Boolean> deleteMessage(Long friendId);

    /**
     * 回复消息
     * @return
     */
    DeveloperResult replyMessage(Long id,SendMessageRequestDTO dto);

    /**
     * 收藏消息
     * @return
     */
    DeveloperResult collectionMessage(Long messageId);

    /**
     * 转发消息
     * @return
     */
    DeveloperResult forwardMessage(Long messageId, List<Long> userIdList);
}
