package com.developer.payment.interceptor;

import com.developer.framework.utils.TokenUtil;
import com.developer.payment.client.SSOClient;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Autowired
    private SSOClient ssoClient;

    @Override
    public void apply(RequestTemplate template) {
        String token = TokenUtil.getToken();
        if(Objects.equals(token, "")){
            Map<String, Object> response = ssoClient.getToken("client_credentials", "client_dev", "dev");
            token = response.get("token_type").toString()+" "+response.get("access_token").toString();
        }
        if (StringUtils.hasText(token)) {
            template.header("Authorization", token);
        }
    }
}
