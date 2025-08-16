package com.developer.im.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.framework.utils.IPUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.im.dto.PushMessageBodyDTO;
import com.developer.im.dto.PushMessageBodyDataDTO;
import com.developer.im.rpc.RpcUtil;
import com.developer.rpc.service.im.IMRpcService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Aspect
public class MessageRouteAspect {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Environment environment;

    @Around("execution(* com.developer.im.listener.processor.IMMessageProcessor.processor(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        PushMessageBodyDTO dto =(PushMessageBodyDTO)args[0];
        PushMessageBodyDataDTO pushMessageBodyDataDTO = dto.parseData(PushMessageBodyDataDTO.class);

        String localIPv4 = IPUtils.getLocalIPv4();
        String localPort = Optional.ofNullable(environment.getProperty("local.server.port")).orElse("8080");
        String localUrl = localIPv4.concat(localPort);

        /** 格式 Map<URL,[user1,user2,user3]></>*/
        Map<String,List<Long>> transpondMap = new HashMap<>();
        List<Long> removeList = new ArrayList<>();


        List<Integer> terminals = MessageTerminalTypeEnum.codes();
        List<Long> receiverIds = pushMessageBodyDataDTO.getReceiverIds();
        for (Long receiverId : receiverIds) {
            int existTerminalCnt = 0;
            for (Integer terminal : terminals) {
                String key = RedisKeyConstant.USER_MAP_SERVER_INFO_KEY(receiverId);
                Object serverUrl = redisUtil.hGet(key, terminal.toString());
                if(ObjectUtil.isEmpty(serverUrl)){
                    continue;
                }

                // 目标节点
                String targetUrl = Optional.ofNullable(serverUrl).orElse("").toString();

                // 当前节点不处理
                if(localUrl.equals(targetUrl)){
                    continue;
                }

                existTerminalCnt++;

                if(transpondMap.containsKey(targetUrl)){
                    List<Long> ids = transpondMap.get(targetUrl);
                    if(ids.contains(receiverId)){
                       continue;
                    }

                    ids.add(receiverId);
                    continue;
                }

                transpondMap.put(targetUrl, Collections.singletonList(receiverId));
            }

            // 记录用户任何terminal都不在此server上的receiverId
            if(existTerminalCnt>=0){
                removeList.add(receiverId);
            }
        }

        pushMessageBodyDataDTO.getReceiverIds().removeAll(removeList);

        if(CollUtil.isEmpty(pushMessageBodyDataDTO.getReceiverIds())){
            return null;
        }


        // 调用目标服务端
        if(MapUtil.isNotEmpty(transpondMap)){
            for (Map.Entry<String,List<Long>> entry : transpondMap.entrySet()){
                String targetUrl = entry.getKey();
                // 远程调用目标server,可以通过Dubbo、feign都可以
                IMRpcService instance = RpcUtil.getInstance(IMRpcService.class, targetUrl);
                instance.pushTargetWSNode(new RabbitMQMessageBodyDTO());
            }
        }

        return joinPoint.proceed();
    }

}
