package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="developer-gateway",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {


    @GetMapping("friendmodule/api/friend/{friendId}/isfriend/{userId}")
    DeveloperResult isFriend(@PathVariable("friendId") Long friendId, @PathVariable("userId") Long userId);


}
