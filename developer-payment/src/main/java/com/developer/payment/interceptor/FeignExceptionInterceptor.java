package com.developer.payment.interceptor;

import com.developer.framework.exception.RemoteInvokeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class FeignExceptionInterceptor {


    @Pointcut("execution(* com.developer.payment.client..*.*(..))")
    public void client(){}

    @AfterThrowing(pointcut = "client()",throwing = "ex")
    public void FeignExceptionHandler(Exception ex){
        log.error("payment服务Feign调用异常,错误内容: {}",ex.getMessage());
        throw new RemoteInvokeException(500,"用户模块内部服务调用异常");
    }


}
