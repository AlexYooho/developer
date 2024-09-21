package com.developer.sso.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

//@FeignClient(name="developer-gateway",url = "/sso-module/api")
@FeignClient(name="developer-gateway")
public interface OAuthClient {

    @PostMapping("/sso-module/api/oauth/token")
    Map<String,Object> getToken(@RequestParam("grant_type") String grantType,
                                @RequestParam("username") String username,
                                @RequestParam("password") String password,
                                @RequestParam("client_id") String clientId,
                                @RequestParam("client_secret") String clientSecret);

}
