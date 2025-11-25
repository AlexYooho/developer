package com.developer.friend.client;

import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.MessageInsertDTO;
import com.developer.friend.dto.RemoveMessageRequestDTO;
import com.developer.friend.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="developer-gateway",contextId = "developer-message",configuration = {FeignRequestInterceptor.class})
public interface MessageClient {


    @PostMapping("/message-module/api/message/{type}/add")
    DeveloperResult<Boolean> insertMessage(@PathVariable("type") MessageMainTypeEnum type, @RequestBody MessageInsertDTO dto);

    @DeleteMapping("/message-module/api/message/{type}/remove")
    DeveloperResult<Boolean> removeFriendChatMessage(@PathVariable("type") MessageMainTypeEnum type,@RequestBody RemoveMessageRequestDTO req);
}
