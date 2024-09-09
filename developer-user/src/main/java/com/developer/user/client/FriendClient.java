package com.developer.user.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.FriendInfoDTO;
import com.developer.user.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="developer-friend",configuration = {FeignRequestInterceptor.class})
public interface FriendClient {

    @GetMapping("/friend/list")
    DeveloperResult friends();

    @PutMapping("/friend/update")
    DeveloperResult modifyFriend(@RequestBody List<FriendInfoDTO> list);

}