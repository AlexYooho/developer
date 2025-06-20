package com.developer.framework.interceptor;

import com.developer.framework.utils.SerialNoHolder;
import com.developer.framework.utils.SnowflakeNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SerialNoInterceptor implements HandlerInterceptor {

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String serialNo = request.getHeader("serial_no");
        if (serialNo == null || serialNo.isEmpty()) {
            serialNo = request.getParameter("serial_no");
        }

        if (serialNo == null || serialNo.isEmpty()) {
            serialNo = snowflakeNoUtil.getSerialNo();
        }

        SerialNoHolder.setSerialNo(serialNo); // 可能是 null，也没关系
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SerialNoHolder.clear();
    }
}
