package com.developer.im.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.MessageTerminalTypeEnum;
import com.developer.framework.utils.IPUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.im.dto.PushMessageBodyDTO;
import com.developer.im.dto.PushMessageBodyDataDTO;
import com.developer.rpc.service.im.IMRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Aspect
public class ClientMapServerInfoAspect {

    @Autowired
    private RedisUtil redisUtil;

    @DubboReference
    private IMRpcService imRpcService;

    @Around("execution(* com.developer.im.listener.processor.IMMessageProcessor.processor(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        PushMessageBodyDTO dto =(PushMessageBodyDTO)args[0];
        PushMessageBodyDataDTO pushMessageBodyDataDTO = dto.parseData(PushMessageBodyDataDTO.class);

        String localIPv4 = IPUtils.getLocalIPv4();

        /** 格式 Map<IP,[user1,user2,user3]></>*/
        Map<String,List<Long>> transpondMap = new HashMap<>();
        List<Long> removeList = new ArrayList<>();
        int notExistTerminalCnt = 0;

        List<Integer> terminals = MessageTerminalTypeEnum.codes();
        List<Long> receiverIds = pushMessageBodyDataDTO.getReceiverIds();
        for (Long receiverId : receiverIds) {
            for (Integer terminal : terminals) {
                String key = RedisKeyConstant.USER_MAP_SERVER_INFO_KEY(receiverId);
                Object serverIP = redisUtil.hGet(key, terminal.toString());
                String ip = Optional.ofNullable(serverIP).orElse("").toString();
                if(!localIPv4.equals(ip)){
                    notExistTerminalCnt++;

                    if(!transpondMap.containsKey(ip)){
                        List<Long> ids = new ArrayList<>();
                        ids.add(receiverId);
                        transpondMap.put(ip,ids);
                    }else{
                        List<Long> ids = transpondMap.get(ip);
                        if(!ids.contains(receiverId)){
                            ids.add(receiverId);
                        }
                    }
                }
            }

            // 如果都不在此server上需要移除掉
            if(notExistTerminalCnt>=terminals.size()){
                removeList.add(receiverId);
            }

            notExistTerminalCnt = 0;
        }

        pushMessageBodyDataDTO.getReceiverIds().removeAll(removeList);


        // 调用目标服务端
        if(MapUtil.isNotEmpty(transpondMap)){
            for (Map.Entry<String,List<Long>> entry : transpondMap.entrySet()){
                String ip = entry.getKey();
                // 远程调用目标server,可以通过dubbo、feign都可以
                imRpcService.pushTargetWSNode();
            }
        }


        if(CollUtil.isEmpty(pushMessageBodyDataDTO.getReceiverIds())){
            return null;
        }

        return joinPoint.proceed();
    }

}
