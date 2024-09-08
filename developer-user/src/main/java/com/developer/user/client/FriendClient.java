package com.developer.user.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.FriendInfoDTO;
import com.developer.user.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="developer-gateway",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("friend-module/api/friend/list")
    DeveloperResult friends();

    @PutMapping("/update")
    DeveloperResult modifyFriend(@RequestBody List<FriendInfoDTO> list);

}
