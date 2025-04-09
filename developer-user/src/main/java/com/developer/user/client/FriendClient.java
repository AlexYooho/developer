package com.developer.user.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.BatchModifyFriendListRequestDTO;
import com.developer.user.dto.FriendInfoDTO;
import com.developer.user.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {
    @GetMapping("/friend-module/api/friend/list")
    DeveloperResult<List<FriendInfoDTO>> friends(@RequestParam("serial_no") String serialNo);

    @PutMapping("/friend/update/list")
    DeveloperResult<Boolean> modifyFriend(@RequestBody BatchModifyFriendListRequestDTO req);

}
