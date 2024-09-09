package com.developer.friend.client;

import com.developer.framework.model.DeveloperResult;
import com.developer.friend.dto.MessageInsertDTO;
import com.developer.friend.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="developer-gateway",configuration = {FeignRequestInterceptor.class})
public interface MessageClient {


    @PostMapping("{type}/add")
    DeveloperResult insertMessage(@PathVariable Integer type, @RequestBody MessageInsertDTO dto);

}
