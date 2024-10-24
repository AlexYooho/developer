package com.developer.framework.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class TokenUtil {


    /**
     * 获取token
     * @return
     */
    public static String getToken(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attr = (ServletRequestAttributes) requestAttributes;
        if(attr==null){
            return "";
        }
        HttpServletRequest request = attr.getRequest();
        return request.getHeader("Authorization");//网关传过来的 token
    }


}
