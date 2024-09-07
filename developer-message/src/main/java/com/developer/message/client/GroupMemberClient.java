package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupMemberClient {

    @GetMapping("get-self-groups")
    DeveloperResult findSelfAllGroup();

}
