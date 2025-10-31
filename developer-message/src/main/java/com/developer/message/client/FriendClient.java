package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.FriendInfoDTO;
import com.developer.message.interceptor.FeignRequestInterceptor;
import com.developer.message.param.IsFriendParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @PostMapping("/friend-module/api/friend/check")
    DeveloperResult<FriendInfoDTO> isFriend(@RequestBody IsFriendParam param);

    @GetMapping("/friend-module/api/friend/list")
    DeveloperResult<List<FriendInfoDTO>> friends(@RequestParam("serial_no") String serialNo);

}
