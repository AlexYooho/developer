package com.developer.payment.interceptor;

import com.developer.framework.exception.RemoteInvokeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class FeignExceptionInterceptor {


    @Pointcut("execution(* com.developer.payment.client..*.*(..))")
    public void client(){}

//    @AfterThrowing(pointcut = "client()",throwing = "ex")
//    public void FeignExceptionHandler(Exception ex){
//        log.error("payment服务Feign调用异常,错误内容: {}",ex.getMessage());
//        throw new RemoteInvokeException(500,"用户模块内部服务调用异常");
//    }

    @Around("client()")
    public Object wrapFeignException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            log.error("Feign调用异常: {}", ex.getMessage(), ex);
            throw ex; // 原样抛出，才能被 Seata 捕捉到
        }
    }
}
