package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.GroupInfoDTO;
import com.developer.message.dto.SelfJoinGroupInfoDTO;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupInfoClient {

    @GetMapping("/group-module/api/group/get-self-join-all-group-info")
    DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(@RequestParam("serialNo") String serialNo);

    @GetMapping("/group-module/api/group/find/{groupId}")
    DeveloperResult<GroupInfoDTO> findGroup(@PathVariable("groupId") Long groupId,@RequestParam("serialNo") String serialNo);

}
