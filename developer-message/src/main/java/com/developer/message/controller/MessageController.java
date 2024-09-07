package com.developer.message.controller;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageServiceRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("message")
public class MessageController {

//    @Autowired
//    private MessageServiceFactoryRegistry registry;

    @Autowired
    private MessageServiceRegister messageServiceRegister;

    /**
     * 发送群聊消息
     * @param req
     * @return
     */
    @PostMapping("/{type}/send")
    public DeveloperResult sendMessage(@PathVariable("type") Integer type,@RequestBody SendMessageRequestDTO req){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).sendMessage(req);
    }

    /**
     * 撤回消息
     * @param id
     * @return
     */
    @PostMapping("/{type}/recall/{id}")
    public DeveloperResult recallMessage(@PathVariable("type") Integer type,@PathVariable Long id){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).recallMessage(id);
    }

    /**
     * 拉取消息
     * @param minId
     * @return
     */
    @GetMapping("/{type}/loadMessage")
    public DeveloperResult loadMessage(@PathVariable("type") Integer type,@RequestParam Long minId){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).loadMessage(minId);
    }

    /**
     * 消息已读
     * @param groupId
     * @return
     */
    @PostMapping("/{type}/readed")
    public DeveloperResult readedMessage(@PathVariable("type") Integer type,@RequestParam Long groupId){
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
    public DeveloperResult recallMessage(@PathVariable("type") Integer type,@RequestParam Long groupId,@RequestParam Long page,@RequestParam Long size){
        return messageServiceRegister.getMessageService(MessageMainTypeEnum.fromCode(type)).findHistoryMessage(groupId,page,size);
    }


}
