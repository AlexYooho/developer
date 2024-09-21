package com.developer.friend.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.MessageInsertDTO;
import com.developer.friend.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="developer-gateway",contextId = "developer-message",configuration = {FeignRequestInterceptor.class},url = "/message-module/api")
public interface MessageClient {


    @PostMapping("message/{type}/add")
    DeveloperResult insertMessage(@PathVariable("type") Integer type, @RequestBody MessageInsertDTO dto);

    @DeleteMapping("{type}/remove/{friendId}")
    DeveloperResult removeFriendChatMessage(@PathVariable("type") Integer type,@PathVariable("friendId") Long friendId);
}
