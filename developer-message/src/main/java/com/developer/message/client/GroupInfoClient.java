package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="developer-gateway",contextId = "developer-group",configuration = {FeignRequestInterceptor.class},url = "/group-module/api")
public interface GroupInfoClient {

    @GetMapping("group/get-self-join-all-group-info")
    DeveloperResult getSelfJoinAllGroupInfo();

    @GetMapping("group/find/{groupId}")
    DeveloperResult findGroup(@PathVariable Long groupId);

}
