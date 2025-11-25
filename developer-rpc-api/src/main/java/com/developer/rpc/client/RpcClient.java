package com.developer.rpc.client;

import com.developer.rpc.service.friend.FriendRpcService;
import com.developer.rpc.service.group.GroupRpcService;
import com.developer.rpc.service.im.IMRpcService;
import com.developer.rpc.service.message.MessageRpcService;
import com.developer.rpc.service.oss.OssRpcService;
import com.developer.rpc.service.payment.PaymentRpcService;
import com.developer.rpc.service.sso.SsoRpcService;
import com.developer.rpc.service.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class RpcClient {

    @DubboReference(timeout = 5000)
    public FriendRpcService friendRpcService;
    //
    @DubboReference(timeout = 5000)
    public GroupRpcService groupRpcService;
    //
    // @DubboReference(timeout = 5000)
    // public IMRpcService imRpcService;
    //
    @DubboReference(timeout = 5000)
    public MessageRpcService messageRpcService;
    //
    // @DubboReference(timeout = 5000)
    // public OssRpcService ossRpcService;
    //
    // @DubboReference(timeout = 5000)
    // public PaymentRpcService paymentRpcService;
    //
    // @DubboReference(timeout = 5000)
    // public SsoRpcService ssoRpcService;

    @DubboReference(timeout = 3000)
    public UserRpcService userRpcService;

}
