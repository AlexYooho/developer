package com.developer.framework.filter;

import com.developer.framework.utils.SerialNoHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.PROVIDER, order = -10000)
public class SerialNoProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 从 RpcContext Attachment 中获取 serial_no
        String serialNo = RpcContext.getServerAttachment().getAttachment("serial_no");
        try {
            // 放入当前线程的 ThreadLocal
            SerialNoHolder.setSerialNo(serialNo);
            return invoker.invoke(invocation);
        } finally {
            SerialNoHolder.clear();
        }
    }
}
