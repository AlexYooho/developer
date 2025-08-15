package com.developer.framework.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPUtils {

    /**
     * 获取当前服务器的内网 IPv4 地址
     * @return 内网 IP 地址字符串，如果未找到返回 null
     */
    public static String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                // 排除回环网卡和未启用的网卡
                if (nic.isLoopback() || !nic.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    // 只要 IPv4 且是内网地址
                    if (!addr.isLoopbackAddress()
                            && addr.isSiteLocalAddress()
                            && addr.getHostAddress().indexOf(":") == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // 如果没找到
    }
}
