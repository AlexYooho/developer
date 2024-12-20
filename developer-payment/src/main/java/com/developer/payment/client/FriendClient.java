package com.developer.payment.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.FriendInfoDTO;
import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("/friend-module/api/friend/{friendId}/is-friend/{userId}")
    DeveloperResult<FriendInfoDTO> isFriend(@PathVariable("friendId") Long friendId, @PathVariable("userId") Long userId);


}
