package com.developer.framework.filter;

import com.developer.framework.utils.SerialNoHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.security.core.context.SecurityContextHolder;

@Activate(group = CommonConstants.CONSUMER, order = -10000)
public class SerialNoConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String serialNo = SerialNoHolder.getSerialNo();
        if (serialNo != null) {
            // 放入 attachment，自动随调用传递
            invocation.setAttachment("serial_no", serialNo);
        }
        SecurityContextHolder.clearContext();
        return invoker.invoke(invocation);
    }
}
