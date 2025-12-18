package com.developer.framework.utils;

import org.apache.dubbo.config.ReferenceConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcUtil {

    private static final Map<String, ReferenceConfig<?>> REFERENCE_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 RPC 服务实例
     * 
     * @param interfaceClass 接口类
     * @param address        服务地址 (ip:port) 或仅 ip (默认端口 20880)
     */
    public static <T> T getInstance(Class<T> interfaceClass, String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("服务地址不能为空");
        }
        if (!address.contains(":")) {
            address = address + ":20880";
        }
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("验证服务地址格式: " + address);
        }
        return getInstance(interfaceClass, parts[0], Integer.parseInt(parts[1]));
    }

    /**
     * 获取 RPC 服务实例
     * 
     * @param interfaceClass 接口类
     * @param ip             服务 IP
     * @param port           服务端口
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> interfaceClass, String ip, int port) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP不能为空");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("端口校验: " + port);
        }

        String key = interfaceClass.getName() + "@" + ip + ":" + port;

        ReferenceConfig<T> referenceConfig = (ReferenceConfig<T>) REFERENCE_CACHE.computeIfAbsent(key, k -> {
            ReferenceConfig<T> reference = new ReferenceConfig<>();
            reference.setInterface(interfaceClass);
            reference.setUrl("dubbo://" + ip + ":" + port);
            reference.setCheck(false);
            reference.setTimeout(5000);
            reference.setRetries(1);

            reference.setConnections(10);
            reference.setLoadbalance("roundrobin");

            return reference;
        });

        try {
            return referenceConfig.get();
        } catch (Exception e) {
            REFERENCE_CACHE.remove(key);
            throw e;
        }
    }

}
