package com.developer.im.common.impl;

import com.developer.im.common.PushMessageToRemoteNodeService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class PushMessageToRemoteNodeServiceImpl implements PushMessageToRemoteNodeService {
    @Override
    public boolean pushMessageToRemoteNode() {
        return false;
    }
}
