package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.FriendInfoDTO;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("/friend-module/api/friend/{friendId}/is-friend/{userId}")
    DeveloperResult<FriendInfoDTO> isFriend(@PathVariable("friendId") Long friendId, @PathVariable("userId") Long userId);

    @GetMapping("/friend-module/api/friend/list")
    DeveloperResult<List<FriendInfoDTO>> friends();

}
