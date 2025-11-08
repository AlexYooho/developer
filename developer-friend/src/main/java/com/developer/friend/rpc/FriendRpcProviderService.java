package com.developer.friend.rpc;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.rpc.service.friend.FriendRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@Component
@RequiredArgsConstructor
public class FriendRpcProviderService implements FriendRpcService {
    @Override
    public DeveloperResult<Boolean> test() {
        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
