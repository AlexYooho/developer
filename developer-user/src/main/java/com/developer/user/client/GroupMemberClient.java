package com.developer.user.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.SelfJoinGroupInfoDTO;
import com.developer.user.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name="developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupMemberClient {


    @GetMapping("/group/get-self-join-all-group-info")
    DeveloperResult getSelfJoinAllGroupInfo();

    @PutMapping("/group-member/update/list")
    DeveloperResult batchModifyGroupMemberInfo(@RequestBody List<SelfJoinGroupInfoDTO> list);

}
