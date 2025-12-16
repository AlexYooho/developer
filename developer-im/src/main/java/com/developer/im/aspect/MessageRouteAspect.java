package com.developer.im.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.dto.ChatMessageDTO;
import com.developer.framework.dto.RabbitMQMessageBodyDTO;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.utils.IPUtils;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.RpcUtil;
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

    @Around("@annotation(com.developer.im.annotations.MessageRouterAspect)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 解析参数
        Object[] args = joinPoint.getArgs();
        RabbitMQMessageBodyDTO dto = (RabbitMQMessageBodyDTO) args[0];
        ChatMessageDTO chatMessageDTO = dto.parseData(ChatMessageDTO.class);

        // 获取当前服务器的地址信息
        String localIPv4 = IPUtils.getLocalIPv4();
        String localPort = Optional.ofNullable(environment.getProperty("dubbo.protocol.port")).orElse("20880");
        String localUrl = localIPv4.concat(":").concat(localPort);

        // 转发的节点信息 格式 Map<URL,[user1,user2,user3]>
        Map<String, List<Long>> transpondMap = new HashMap<>();
        List<Long> removeList = new ArrayList<>();

        // 遍历接收消息的目标对象
        for (Long targetId : chatMessageDTO.getTargetIds()) {
            int existTerminalCnt = 0;
            String key = RedisKeyConstant.USER_MAP_SERVER_INFO_KEY(targetId);
            // 遍历当前接收对象的不同终端
            for (Integer terminal : TerminalTypeEnum.codes()) {
                // 判断当前接收对象的当前终端是否在线
                Object serverUrl = redisUtil.hGet(key, terminal.toString());
                if (ObjectUtil.isEmpty(serverUrl)) {
                    // 不在线则不需要处理
                    continue;
                }

                // 在线-目标节点
                String targetUrl = Optional.ofNullable(serverUrl).orElse("").toString();

                // 当前节点不处理
                if (localUrl.equals(targetUrl)) {
                    continue;
                }

                existTerminalCnt++;

                // 记录需要做路由转发的信息
                if (!transpondMap.containsKey(targetUrl)) {
                    transpondMap.put(targetUrl, Collections.singletonList(targetId));
                    continue;
                }

                List<Long> ids = transpondMap.get(targetUrl);
                if (ids.contains(targetId)) {
                    continue;
                }
                ids.add(targetId);
            }

            // 记录用户任何terminal都不在此server上的targetId
            if (existTerminalCnt >= 0) {
                removeList.add(targetId);
            }
        }

        chatMessageDTO.getTargetIds().removeAll(removeList);

        if (CollUtil.isEmpty(chatMessageDTO.getTargetIds())) {
            return null;
        }

        if (MapUtil.isEmpty(transpondMap)) {
            return joinPoint.proceed();
        }

        // 调用目标服务端
        for (Map.Entry<String, List<Long>> entry : transpondMap.entrySet()) {
            String targetUrl = entry.getKey();
            // 远程调用目标server,可以通过Dubbo、feign都可以
            IMRpcService instance = RpcUtil.getInstance(IMRpcService.class, targetUrl);
            chatMessageDTO.setTargetIds(entry.getValue());
            dto.setData(chatMessageDTO);
            instance.pushTargetWSNode(dto);
        }

        return joinPoint.proceed();
    }

}
