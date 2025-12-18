package com.developer.framework.filter;

import com.alibaba.fastjson.JSON;
import com.developer.framework.model.SelfUserInfoModel;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Activate(group = { CommonConstants.CONSUMER, CommonConstants.PROVIDER }, order = -9000)
public class UserInfoContextFilter implements Filter {

    private static final String USER_INFO_KEY = "user_info";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (isProviderSide()) {
            String userInfoJson = invocation.getAttachment(USER_INFO_KEY);
            if (userInfoJson != null) {
                SelfUserInfoModel userInfo = JSON.parseObject(userInfoJson, SelfUserInfoModel.class);
                if (userInfo != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userInfo.getUserName(), "N/A", null);
                    authentication.setDetails(userInfoJson);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            if (isProviderSide()) {
                SecurityContextHolder.clearContext();
            }
        }
    }

    protected boolean isProviderSide() {
        return RpcContext.getServiceContext().isProviderSide();
    }
}
