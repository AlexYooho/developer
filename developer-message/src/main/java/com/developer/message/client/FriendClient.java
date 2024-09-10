package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class},url = "/friend-module/api")
public interface FriendClient {

    @GetMapping("friend/{friendId}/is-friend/{userId}")
    DeveloperResult isFriend(@PathVariable("friendId") Long friendId, @PathVariable("userId") Long userId);

    @GetMapping("friend/list")
    DeveloperResult friends();

}
