package com.developer.message.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.FindGroupRequestDTO;
import com.developer.message.dto.GroupInfoDTO;
import com.developer.message.dto.SelfJoinGroupInfoDTO;
import com.developer.message.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupInfoClient {

    @GetMapping("/group-module/api/group/get-self-join-all-group-info")
    DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(@RequestParam("serial_no") String serialNo);

    @PostMapping("/group-module/api/group/find")
    DeveloperResult<GroupInfoDTO> findGroup(@RequestBody FindGroupRequestDTO req);

}
