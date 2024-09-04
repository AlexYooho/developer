package com.developer.im.netty.service;

public interface IMServer {

    /**
     * IM服务是否准备就绪
     * @return
     */
    boolean isReady();

    /**
     * 启动IM服务
     */
    void start();

    /**
     * 停止IM服务
     */
    void stop();

}
