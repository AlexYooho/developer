package com.developer.group.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.group.dto.FriendInfoDTO;
import com.developer.group.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("/friend-module/api/friend/list")
    DeveloperResult<List<FriendInfoDTO>> friends(@RequestParam("serial_no") String serialNo);

}
