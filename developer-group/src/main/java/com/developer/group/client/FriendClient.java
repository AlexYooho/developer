package com.developer.group.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="developer-gateway",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("friend-module/api/friend/list")
    DeveloperResult friends();

}
