package com.developer.framework.utils;

public class SerialNoHolder {

    private static final ThreadLocal<String> serialNoThreadLocal = new ThreadLocal<>();

    public static void setSerialNo(String serialNo){
        serialNoThreadLocal.set(serialNo);
    }

    public static String getSerialNo() {
        return serialNoThreadLocal.get();
    }

    public static void clear() {
        serialNoThreadLocal.remove();
    }
}
