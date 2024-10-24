package com.developer.payment.client;

import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="developer-gateway",contextId = "developer-sso")
public interface SSOClient {

    @GetMapping("/sso-module/api/oauth/token")
    Map<String,Object> getToken(@RequestParam("grant_type") String grantType,
                                @RequestParam("client_id") String clientId,
                                @RequestParam("client_secret") String clientSecret);

}
