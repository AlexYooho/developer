package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="developer-gateway",contextId = "developer-group-member",configuration = {FeignRequestInterceptor.class})
public interface GroupMemberClient {

    @GetMapping("/group-module/api/group-member/group/{groupId}/get-group-member-user-id")
    DeveloperResult findGroupMemberUserId(@PathVariable("groupId") Long groupId);
}
