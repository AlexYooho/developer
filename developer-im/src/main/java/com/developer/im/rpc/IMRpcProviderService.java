package com.developer.im.rpc;

import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.model.DeveloperResult;
import com.developer.im.listener.processor.IMMessageProcessor;
import com.developer.rpc.service.im.IMRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@DubboService
@Component
public class IMRpcProviderService implements IMRpcService {

    @Autowired
    private IMMessageProcessor processor;

    @Autowired
    private Environment environment;

    @Override
    public DeveloperResult<Boolean> pushTargetWSNode(RabbitMQMessageBodyDTO dto) {
        return processor.processor(dto);
    }

    @Override
    public String sayHi(String name) {
        return name.concat(":hi,port:").concat(Objects.requireNonNull(environment.getProperty("dubbo.protocol.port")));
    }

}
