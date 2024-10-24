package com.developer.user.interceptor;

import com.developer.framework.utils.TokenUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String token = TokenUtil.getToken();
        if (StringUtils.hasText(token)) {
            template.header("Authorization", token);
        }
    }
}
