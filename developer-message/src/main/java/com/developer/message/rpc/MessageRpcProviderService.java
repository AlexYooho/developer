package com.developer.message.rpc;

import com.developer.rpc.service.message.MessageRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@Component
public class MessageRpcProviderService implements MessageRpcService {
    @Override
    public String sayHi(String name) {
        return "hi:".concat(name);
    }
}
