package com.developer.framework.exception;

import com.developer.framework.model.DeveloperResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionProcessor {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public DeveloperResult<String> processor(Exception e){
        if(e instanceof RemoteInvokeException){
            RemoteInvokeException invokeException = (RemoteInvokeException) e;
            return DeveloperResult.error(invokeException.getCode(),invokeException.getErrMsg());
        }
        log.error(e.toString());
        return DeveloperResult.error("服务器出错辣~~~~,快速修复中,请耐心等待");
    }

}
