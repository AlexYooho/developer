package com.developer.message.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.SendMessageRequestDTO;
import com.developer.message.service.MessageServiceFactoryRegistry;
import com.developer.message.service.MessageServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("message")
public class MessageController {

    @Autowired
    private MessageServiceFactoryRegistry registry;

    /**
     * 发送群聊消息
     * @param req
     * @return
     */
    @PostMapping("/send")
    public DeveloperResult sendMessage(@RequestBody SendMessageRequestDTO req){
        return registry.getFactory(MessageServiceType.GROUP).createMessageService().sendMessage(req);
    }

    /**
     * 撤回消息
     * @param id
     * @return
     */
    @PostMapping("/recall/{id}")
    public DeveloperResult recallMessage(@PathVariable Long id){
        return registry.getFactory(MessageServiceType.GROUP).createMessageService().recallMessage(id);
    }

    /**
     * 拉取消息
     * @param minId
     * @return
     */
    @GetMapping("/loadMessage")
    public DeveloperResult loadMessage(@RequestParam Long minId){
        return registry.getFactory(MessageServiceType.GROUP).createMessageService().loadMessage(minId);
    }

    /**
     * 消息已读
     * @param groupId
     * @return
     */
    @PostMapping("/readed")
    public DeveloperResult readedMessage(@RequestParam Long groupId){
        return registry.getFactory(MessageServiceType.GROUP).createMessageService().readMessage(groupId);
    }

    /**
     * 查询聊天记录
     * @param groupId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/history")
    public DeveloperResult recallMessage(@RequestParam Long groupId,@RequestParam Long page,@RequestParam Long size){
        return registry.getFactory(MessageServiceType.GROUP).createMessageService().findHistoryMessage(groupId,page,size);
    }


}
