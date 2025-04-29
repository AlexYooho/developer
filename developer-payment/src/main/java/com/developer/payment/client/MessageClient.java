package com.developer.payment.client;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.CheckVerifyCodeRequestDTO;
import com.developer.payment.dto.FriendInfoDTO;
import com.developer.payment.dto.IsFriendDto;
import com.developer.payment.dto.SendMessageRequestDTO;
import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="developer-gateway",contextId = "developer-message",configuration = {FeignRequestInterceptor.class})
public interface MessageClient {

    @GetMapping("/message-module/api/verify-code/check")
    DeveloperResult<Boolean> verifyCodeCheck(@RequestBody CheckVerifyCodeRequestDTO req);

    @PostMapping("/message-module/api/message/{type}/send")
    DeveloperResult sendMessage(@PathVariable("type")MessageMainTypeEnum type, @RequestBody SendMessageRequestDTO req);

}
