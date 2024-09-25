package com.developer.sso.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

@Component
public class CustomOauth2ExceptionHandler implements WebResponseExceptionTranslator<OAuth2Exception> {

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
        // 处理具体的异常类型
        if (e instanceof InvalidGrantException) {
            // 处理密码或其他密钥校验失败
            OAuth2Exception badCredentialsException = new OAuth2Exception("用户名或密码错误");
            return ResponseEntity
                    .status(500) // 返回 HTTP 400 错误码
                    .body(badCredentialsException);
        }

        // 处理其他类型的异常
        OAuth2Exception oAuth2Exception = new OAuth2Exception(e.getMessage());
        return ResponseEntity
                .status(500) // 返回 HTTP 500 错误码，表示服务器内部错误
                .body(oAuth2Exception);
    }
}
