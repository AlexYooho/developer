package com.developer.payment.client;

import com.developer.framework.enums.message.MessageMainTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.CheckVerifyCodeRequestDTO;
import com.developer.payment.dto.SendMessageRequestDTO;
import com.developer.payment.dto.SendRedPacketsResultDTO;
import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="developer-gateway",contextId = "developer-message",configuration = {FeignRequestInterceptor.class})
public interface MessageClient {

    @GetMapping("/message-module/api/verify-code/check")
    DeveloperResult<Boolean> verifyCodeCheck(@RequestBody CheckVerifyCodeRequestDTO req);

    @PostMapping("/message-module/api/message/{type}/send")
    DeveloperResult<SendRedPacketsResultDTO> sendMessage(@RequestHeader("serial_no") String serialNo, @PathVariable("type")MessageMainTypeEnum type, @RequestBody SendMessageRequestDTO req);

}
