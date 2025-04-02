package com.developer.framework.exception;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomIdentityVerifyExceptionProcessor extends OAuth2AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        DeveloperResult<Boolean> result;

        // 根据异常消息自定义返回文案
        String message = authException.getMessage().toLowerCase();

        if (message.contains("full authentication is required to access this resource") || message.contains("authentication token not found")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"请提供有效的访问令牌");
        } else if (message.contains("invalid token") || message.contains("malformed")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"令牌格式错误或无效");
        } else if (message.contains("expired")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"登录过期,请重新登录");
        } else if (message.contains("signature")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"令牌签名验证失败");
        } else if (message.contains("resource id")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"令牌资源ID不匹配");
        } else if (message.contains("scope")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"权限范围不足");
        } else if (message.contains("token type")) {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),401,"令牌类型错误");
        } else {
            result = DeveloperResult.error(snowflakeNoUtil.getSerialNo(),500,"权限认证失败: " + authException.getMessage());
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}