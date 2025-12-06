package com.developer.message.service;

import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;

import java.util.List;

public interface BasicMessageService {

    /**
     * 消息主体类型
     * @return
     */
    MessageMainTypeEnum messageMainType();

    /**
     * 加载消息
     * @param req
     * @return
     */
    DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(LoadMessageRequestDTO req);

    /**
     * 发送消息
     * @param req
     * @return
     */
    DeveloperResult<SendMessageResultDTO> sendMessage(SendMessageRequestDTO req);

    /**
     * 已读消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> readMessage(ReadMessageRequestDTO req);

    /**
     * 撤回消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> withdrawMessage(WithdrawMessageRequestDTO req);

    /**
     * 新增消息
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> insertMessage(MessageInsertDTO dto);

    /**
     * 删除消息
     * @param req
     * @return
     */
    DeveloperResult<Boolean> deleteMessage(RemoveMessageRequestDTO req);

    /**
     * 查询历史记录
     * @param req
     * @return
     */
    DeveloperResult<List<QueryHistoryMessageResponseDTO>> findHistoryMessage(QueryHistoryMessageRequestDTO req);

}
