package com.developer.rpc.client;

import com.alibaba.fastjson.JSON;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.SelfUserInfoModel;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

public class RpcExecutor {

    public static <T> T execute(Supplier<T> call) {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            try {
                // 提取用户信息并放入 Attachment
                SelfUserInfoModel userInfo = SelfUserInfoContext.selfUserInfo();
                if (userInfo != null) {
                    RpcContext.getContext().setAttachment("user_info", JSON.toJSONString(userInfo));
                }
            } catch (Exception e) {
                // 忽略异常，防止影响主流程
            }

            // 清除上下文，防止 Dubbo 序列化 OAuth2Authentication
            SecurityContextHolder.clearContext();
            return call.get();
        } finally {
            SecurityContextHolder.setContext(context);
        }
    }

}
