package com.developer.im.netty.service;

import com.developer.framework.constant.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
@Component
public class IMStartServer implements CommandLineRunner {

    /**
     * 服务id
     */
    public static volatile long serverId=0;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private List<IMServer> imServers;

    @Override
    public void run(String... args) throws Exception {
        // 初始化SERVER_ID
        String key = RedisKeyConstant.IM_MAX_SERVER_ID;
        serverId =  redisTemplate.opsForValue().increment(key,1);
        // 启动服务
        for(IMServer imServer:imServers){
            imServer.start();
        }
    }

    @PreDestroy
    public void destory(){
        // 停止服务
        for(IMServer imServer:imServers){
            imServer.stop();
        }
    }
}
