package com.developer.framework.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
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
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes attr = (ServletRequestAttributes) requestAttributes;
//        if(attr==null){
//            return "";
//        }
//        HttpServletRequest request = attr.getRequest();
//        return request.getHeader("Authorization");//网关传过来的 token
        return "Bearer "+((OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getTokenValue();
    }


}
