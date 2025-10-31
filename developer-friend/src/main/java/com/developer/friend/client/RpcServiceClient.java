package com.developer.friend.client;

import com.developer.rpc.service.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class RpcServiceClient {

    @DubboReference(timeout = 3000)
    public UserRpcService userRpcService;

}
