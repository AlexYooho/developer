package com.developer.group.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class},url = "/friend-module/api")
public interface FriendClient {

    @GetMapping("friend/list")
    DeveloperResult friends();

}
