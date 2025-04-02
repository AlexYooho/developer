package com.developer.framework.exception;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionProcessor {

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public DeveloperResult<String> processor(Exception e){
        if(e instanceof RemoteInvokeException){
            RemoteInvokeException invokeException = (RemoteInvokeException) e;
            return DeveloperResult.error(snowflakeNoUtil.getSerialNo(),invokeException.getCode(),invokeException.getErrMsg());
        }

        if(e instanceof EmailException){
            EmailException emailException = (EmailException) e;
            return DeveloperResult.error(snowflakeNoUtil.getSerialNo(),emailException.getCode(),emailException.getErrMsg());
        }

        log.error(e.toString());
        throw new RuntimeException(e);
        //return DeveloperResult.error("服务器出错辣~~~~,快速修复中,请耐心等待");
    }

}
