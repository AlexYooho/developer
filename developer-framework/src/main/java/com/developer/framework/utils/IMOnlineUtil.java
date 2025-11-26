package com.developer.framework.utils;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.common.TerminalTypeEnum;
import com.developer.framework.model.IMUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IMOnlineUtil {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public Boolean isOnline(Long userId){
        String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, userId.toString(), "*");
        return !redisTemplate.keys(key).isEmpty();
    }


    public List<Long> getOnlineUser(List<Long> userIds){
        return new LinkedList<>(getOnlineTerminal(userIds).keySet());
    }


    public Map<Long,List<TerminalTypeEnum>> getOnlineTerminal(List<Long> userIds){
        if(userIds.isEmpty()){
            return Collections.EMPTY_MAP;
        }
        // 把所有用户的key都存起来
        Map<String, IMUserInfo> userMap = new HashMap<>();
        for(Long id:userIds){
            for (Integer terminal : TerminalTypeEnum.codes()) {
                String key = String.join(":", RedisKeyConstant.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                userMap.put(key,new IMUserInfo(id,terminal));
            }
        }
        // 批量拉取
        List<Object> serverIds = redisTemplate.opsForValue().multiGet(userMap.keySet());
        int idx = 0;
        Map<Long,List<TerminalTypeEnum>> onlineMap = new HashMap<>();
        for (Map.Entry<String, IMUserInfo> entry : userMap.entrySet()) {
            // serverid有值表示用户在线
            if(serverIds.get(idx++) != null){
                IMUserInfo userInfo = entry.getValue();
                List<TerminalTypeEnum> terminals = onlineMap.computeIfAbsent(userInfo.getId(), o -> new LinkedList<>());
                terminals.add(TerminalTypeEnum.fromCode(userInfo.getTerminal()));
            }
        }
        // 去重并返回
        return onlineMap;
    }

}
