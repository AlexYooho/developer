package com.developer.im.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.rpc.service.im.IMRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class IMRpcProviderService implements IMRpcService {

    @Override
    public DeveloperResult<Boolean> pushTargetWSNode() {
        return null;
    }

}
