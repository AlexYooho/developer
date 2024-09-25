package com.developer.message.controller;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.MessageInsertDTO;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.dto.SendMessageResultDTO;
import com.developer.message.service.MessageServiceRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("message")
public class MessageController {

    @Autowired
    private MessageServiceRegister messageServiceRegister;

    /**
     * 发送群聊消息
     * @param req
     * @return
     */
    @PostMapping("/{type}/send")
    public DeveloperResult<SendMessageResultDTO> sendMessage(@PathVariable("type") Integer type,@RequestBody SendMessageRequestDTO req){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).sendMessage(req);
    }

    /**
     * 撤回消息
     * @param id
     * @return
     */
    @PostMapping("/{type}/recall/{id}")
    public DeveloperResult<Boolean> recallMessage(@PathVariable("type") Integer type,@PathVariable Long id){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).recallMessage(id);
    }

    /**
     * 拉取消息
     * @param minId
     * @return
     */
    @GetMapping("/{type}/loadMessage")
    public DeveloperResult<List<SendMessageResultDTO>> loadMessage(@PathVariable("type") Integer type, @RequestParam Long minId){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).loadMessage(minId);
    }

    /**
     * 消息已读
     * @param groupId
     * @return
     */
    @PostMapping("/{type}/readed")
    public DeveloperResult<Boolean> readedMessage(@PathVariable("type") Integer type,@RequestParam Long groupId){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).readMessage(groupId);
    }

    /**
     * 查询聊天记录
     * @param groupId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{type}/history")
    public DeveloperResult<List<SendMessageResultDTO>> recallMessage(@PathVariable("type") Integer type,@RequestParam Long groupId,@RequestParam Long page,@RequestParam Long size){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).findHistoryMessage(groupId,page,size);
    }

    /**
     * 新增消息
     * @param type
     * @param dto
     * @return
     */
    @PostMapping("{type}/add")
    public DeveloperResult<Boolean> insertMessage(@PathVariable Integer type,@RequestBody MessageInsertDTO dto){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).insertMessage(dto);
    }

    /**
     * 删除消息
     * @param type
     * @param friendId
     * @return
     */
    @DeleteMapping("{type}/remove/{friendId}")
    public DeveloperResult<Boolean> removeFriendChatMessage(@PathVariable Integer type,@PathVariable Long friendId){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).deleteMessage(friendId);
    }

    /**
     * 回复消息
     * @param type
     * @param id
     * @param dto
     * @return
     */
    @PostMapping("{type}/reply/{id}")
    public DeveloperResult<Boolean> replyMessage(@PathVariable Integer type,@PathVariable Long id,@RequestBody SendMessageRequestDTO dto){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).replyMessage(id,dto);
    }

    /**
     * 收藏消息
     * @param type
     * @param id
     * @return
     */
    @PostMapping("{type}/collection/{id}")
    public DeveloperResult<Boolean> collectionMessage(@PathVariable Integer type,@PathVariable Long id) {
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).collectionMessage(id);
    }

    /**
     * 转发消息
     * @param type
     * @param messageId
     * @param userIdList
     * @return
     */
    @PostMapping("{type}/forward/{messageId}")
    public DeveloperResult<Boolean> forwardMessage(@PathVariable Integer type, @PathVariable Long messageId, @RequestBody List<Long> userIdList){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).forwardMessage(messageId,userIdList);
    }

}
