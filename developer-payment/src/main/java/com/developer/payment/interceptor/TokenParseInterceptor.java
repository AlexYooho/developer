package com.developer.payment.interceptor;

import com.developer.framework.dto.MessageBodyDTO;
import com.developer.framework.dto.PaymentInfoDTO;
import com.developer.framework.exception.RemoteInvokeException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Aspect
@Component
@Slf4j
public class TokenParseInterceptor {

    @Pointcut("execution(* com.developer.payment.listener..*.*(..))")
    public void client(){}

    @Before("client()")
    public void FeignExceptionHandler(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        MessageBodyDTO<PaymentInfoDTO> dto = (MessageBodyDTO<PaymentInfoDTO>) args[0];
        Claims claims = Jwts.parser()
                .setSigningKey("developer".getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(dto.getToken().replace("Bearer ",""))
                .getBody();
        PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(claims.getSubject(), dto.getToken(), null);

        authentication.setDetails(claims.get("selfUserInfoKey"));

        // 将 `Authentication` 存入 `SecurityContext`
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
