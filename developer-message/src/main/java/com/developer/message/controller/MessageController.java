package com.developer.message.controller;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;
import com.developer.message.service.factory.MessageTypeProcessorDispatchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("message")
public class MessageController {

    @Autowired
    private MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    /**
     * 发送消息
     * @param req
     * @return
     */
    @PostMapping("/{type}/send")
    public DeveloperResult<SendMessageResultDTO> sendMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody SendMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).sendMessage(req);
    }

    /**
     * 撤回消息
     * @param req
     * @return
     */
    @PostMapping("/{type}/withdraw")
    public DeveloperResult<Boolean> withdrawMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody RecallMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).withdrawMessage(req);
    }

    /**
     * 拉取消息
     * @param
     * @return
     */
    @GetMapping("/{type}/loadMessage/{min_id}")
    public DeveloperResult<List<SendMessageResultDTO>> loadMessage(@PathVariable("type") MessageMainTypeEnum type,@PathVariable("min_id") Long minId,@RequestParam(value = "serial_no",required = false,defaultValue = "") String serialNo) {
        LoadMessageRequestDTO dto = new LoadMessageRequestDTO();
        dto.setMinId(minId);
        dto.setSerialNo(serialNo);
        return messageTypeProcessorDispatchFactory.getInstance(type).loadMessage(dto);
    }

    /**
     * 消息已读
     * @param type
     * @param req
     * @return
     */
    @PostMapping("/{type}/read")
    public DeveloperResult<Boolean> readMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody ReadMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).readMessage(req);
    }

    /**
     * 查询聊天记录
     * @param req
     * @return
     */
    @PostMapping("/{type}/history")
    public DeveloperResult<List<SendMessageResultDTO>> recallMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody QueryHistoryMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).findHistoryMessage(req);
    }

    /**
     * 新增消息
     * @param type
     * @param dto
     * @return
     */
    @PostMapping("{type}/add")
    public DeveloperResult<Boolean> insertMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody MessageInsertDTO dto) {
        return messageTypeProcessorDispatchFactory.getInstance(type).insertMessage(dto);
    }

    /**
     * 删除消息
     * @param type
     * @param req
     * @return
     */
    @DeleteMapping("{type}/remove")
    public DeveloperResult<Boolean> removeFriendChatMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody RemoveMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).deleteMessage(req);
    }

    /**
     * 回复消息
     * @param type
     * @param messageId
     * @param dto
     * @return
     */
    @PostMapping("{type}/reply/{messageId}")
    public DeveloperResult<Boolean> replyMessage(@PathVariable("type") MessageMainTypeEnum type, @PathVariable("messageId") Long messageId, @RequestBody ReplyMessageRequestDTO dto) {
        return messageTypeProcessorDispatchFactory.getInstance(type).replyMessage(messageId, dto);
    }

    /**
     * 收藏消息
     * @param type
     * @param req
     * @return
     */
    @PostMapping("{type}/collection/{messageId}")
    public DeveloperResult<Boolean> collectionMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody CollectionMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).collectionMessage(req);
    }

    /**
     * 转发消息
     * @param type
     * @param req
     * @return
     */
    @PostMapping("{type}/forward")
    public DeveloperResult<Boolean> forwardMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody ForwardMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).forwardMessage(req);
    }

    /**
     * 消息点赞
     * @param type
     * @param req
     * @return
     */
    @PostMapping("{type}/like")
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody MessageLikeRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).likeMessage(req);
    }

    /**
     * 消息取消点赞
     * @param type
     * @param req
     * @return
     */
    @PostMapping("{type}/unlike")
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody MessageLikeRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).unLikeMessage(req);
    }

}
