package com.developer.user.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.SelfJoinGroupInfoDTO;
import com.developer.user.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupMemberClient {
    @GetMapping("/group-module/api/group/get-self-join-all-group-info")
    DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(@RequestParam("serial_no") String serialNo);

    @PutMapping("/group-module/api/group-member/update/list")
    DeveloperResult<Boolean> batchModifyGroupMemberInfo(@RequestBody List<SelfJoinGroupInfoDTO> list);

}
