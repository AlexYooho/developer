package com.developer.payment.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.SelfJoinGroupInfoDTO;
import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="developer-gateway",contextId = "developer-group",configuration = {FeignRequestInterceptor.class})
public interface GroupClient {

    @GetMapping("/group-module/api/group/get-self-join-all-group-info")
    DeveloperResult<List<SelfJoinGroupInfoDTO>> getSelfJoinAllGroupInfo(@RequestParam("serial_no") String serialNo);

}
