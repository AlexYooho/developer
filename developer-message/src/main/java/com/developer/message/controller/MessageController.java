package com.developer.message.controller;

import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.enums.message.MessageConversationTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.*;
import com.developer.message.service.factory.MessageTypeProcessorDispatchFactory;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("message")
@AllArgsConstructor
public class MessageController {

    private final MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    /*
    发送消息
     */
    @PostMapping("/{type}/send")
    public DeveloperResult<SendMessageResultDTO> sendMessage(@PathVariable("type") Integer type, @RequestBody SendMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).sendMessage(req);
    }

    /*
    撤回消息
     */
    @PostMapping("/{type}/withdraw")
    public DeveloperResult<Boolean> withdrawMessage(@PathVariable("type") Integer type, @RequestBody WithdrawMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).withdrawMessage(req);
    }

    /*
    拉取消息
     */
    @GetMapping("/{type}/loadMessage/{terminal_type}/{target_id}/{conv_seq}")
    public DeveloperResult<List<LoadMessageListResponseDTO>> loadMessage(@PathVariable("type") Integer type, @PathVariable("terminal_type") Integer terminalType, @PathVariable("target_id") Long targetId,@PathVariable("conv_seq")Long convSeq) {
        LoadMessageRequestDTO dto = new LoadMessageRequestDTO();
        dto.setTargetId(targetId);
        dto.setTerminalType(TerminalTypeEnum.fromCode(terminalType));
        dto.setLastSeq(convSeq);
        return messageTypeProcessorDispatchFactory.getInstance(type).loadMessage(dto);
    }

    /*
    消息已读
     */
    @PostMapping("/{type}/read")
    public DeveloperResult<Boolean> readMessage(@PathVariable("type") Integer type, @RequestBody ReadMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).readMessage(req);
    }

    /*
    查询聊天记录
     */
    @PostMapping("/{type}/history")
    public DeveloperResult<List<QueryHistoryMessageResponseDTO>> recallMessage(@PathVariable("type") Integer type, @RequestBody QueryHistoryMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).findHistoryMessage(req);
    }

    /*
    新增消息
     */
    @PostMapping("{type}/add")
    public DeveloperResult<Boolean> insertMessage(@PathVariable("type") Integer type, @RequestBody MessageInsertDTO dto) {
        return messageTypeProcessorDispatchFactory.getInstance(type).insertMessage(dto);
    }

    /*
    删除消息
     */
    @PostMapping("{type}/remove")
    public DeveloperResult<Boolean> removeFriendChatMessage(@PathVariable("type") Integer type, @RequestBody RemoveMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).deleteMessage(req);
    }

    /*
    回复消息
     */
    @PostMapping("{type}/reply/{message_id}")
    public DeveloperResult<Boolean> replyMessage(@PathVariable("type") Integer type, @PathVariable("message_id") Long messageId, @RequestBody ReplyMessageRequestDTO dto) {
        return messageTypeProcessorDispatchFactory.getInstance(type).replyMessage(messageId, dto);
    }

    /*
    收藏消息
     */
    @PostMapping("{type}/collection/{messageId}")
    public DeveloperResult<Boolean> collectionMessage(@PathVariable("type") Integer type, @RequestBody CollectionMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).collectionMessage(req);
    }

    /*
    转发消息
     */
    @PostMapping("{type}/forward")
    public DeveloperResult<Boolean> forwardMessage(@PathVariable("type") Integer type, @RequestBody ForwardMessageRequestDTO req) {
        return messageTypeProcessorDispatchFactory.getInstance(type).forwardMessage(req);
    }

    /*
    消息点赞
     */
    @PostMapping("{type}/like/{message_id}")
    public CompletableFuture<DeveloperResult<Boolean>> likeMessage(@PathVariable("type") Integer type, @PathVariable("message_id") Long messageId) {
        return messageTypeProcessorDispatchFactory.getInstance(type).likeMessage(messageId);
    }

    /*
    消息取消点赞
     */
    @PostMapping("{type}/unlike/{message_id}")
    public CompletableFuture<DeveloperResult<Boolean>> unLikeMessage(@PathVariable("type") Integer type, @PathVariable("message_id") Long messageId) {
        return messageTypeProcessorDispatchFactory.getInstance(type).unLikeMessage(messageId);
    }

}
