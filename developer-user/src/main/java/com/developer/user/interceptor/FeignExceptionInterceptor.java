package com.developer.user.interceptor;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FeignExceptionInterceptor {


    @Pointcut("execution(* com.developer.user.client..*.*(..))")
    public void client(){}

    @AfterThrowing(pointcut = "client()",throwing = "ex")
    public void FeignExceptionHandler(Exception ex){
        System.out.println(ex);
    }


}
