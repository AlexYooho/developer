package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.FindGroupMemberUserIdRequestDTO;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-group-member",configuration = {FeignRequestInterceptor.class})
public interface GroupMemberClient {

    @PostMapping("/group-module/api/group-member/get-group-member-user-id")
    DeveloperResult<List<Long>> findGroupMemberUserId(@RequestBody FindGroupMemberUserIdRequestDTO req);
}
