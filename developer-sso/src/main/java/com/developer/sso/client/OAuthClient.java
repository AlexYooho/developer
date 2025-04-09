package com.developer.sso.client;

import feign.Headers;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="developer-gateway")
public interface OAuthClient {

    @PostMapping("/sso-module/api/oauth/token")
    Map<String,Object> getToken(@RequestParam("grant_type") String grantType,@RequestParam("username") String username,@RequestParam("password") String password,@RequestParam("client_id") String clientId,@RequestParam("client_secret") String clientSecret);

    @PostMapping("/sso-module/api/oauth/token")
    Map<String,Object> refreshToken(@RequestParam("client_secret") String clientSecret,@RequestParam("grant_type") String grantType,@RequestParam("refresh_token") String refreshToken,@RequestParam("client_id") String clientId);
}
