package com.developer.framework.utils;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

public class RpcUtil {

    public static <T> T getInstance(Class<T> interfaceClass,String url){
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceClass);
        reference.setUrl("dubbo://".concat(url));
        reference.setCheck(false);
        reference.setTimeout(3000);

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.reference(reference);

        return reference.get();
    }

}
