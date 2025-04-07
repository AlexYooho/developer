package com.developer.message.interceptor;

import com.developer.framework.model.DeveloperResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

//@ControllerAdvice
//public class ResponseBodyInterceptor implements ResponseBodyAdvice<Object> {
//
//    @Override
//    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return DeveloperResult.class.isAssignableFrom(returnType.getParameterType());
//    }
//
//    @Override
//    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//        if(body instanceof DeveloperResult){
//
//            String serialNo = "";
//            List<String> headers = request.getHeaders().get("serialNo");
//            if(headers!=null && !headers.isEmpty()){
//                serialNo = headers.get(0);
//            }
//
//            DeveloperResult<Object> result = (DeveloperResult<Object>) body;
//
//            // 设置 serialNo 到返回结果
//            if(!serialNo.isEmpty()) {
//                result.setSerialNo(serialNo);
//            }
//        }
//        return body;
//    }
//}
