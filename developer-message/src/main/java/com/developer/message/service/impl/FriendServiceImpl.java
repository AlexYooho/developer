package com.developer.message.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.client.FriendClient;
import com.developer.message.dto.FriendInfoDTO;
import com.developer.message.param.IsFriendParam;
import com.developer.message.service.FriendService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendClient client;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 本地缓存
     */
    private final Cache<String,Boolean> localCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();


    @Override
    public DeveloperResult<Boolean> isFriend(Long userId, Long friendId) {
        String key = RedisKeyConstant.IS_FRIEND_KEY(userId,friendId);
        String serialNo = SerialNoHolder.getSerialNo();

        // 1、本地缓存
        Boolean local = localCache.getIfPresent(key);
        if(local!=null){
            return local ? DeveloperResult.success(serialNo) : DeveloperResult.error(serialNo);
        }

        // 2、redis缓存
        Boolean redis = redisUtil.get(key, Boolean.class);
        if(redis!=null){
            localCache.put(key,redis);
            return redis ? DeveloperResult.success(serialNo) : DeveloperResult.error(serialNo);
        }

        // 3、远程调用
        DeveloperResult<FriendInfoDTO> result = client.isFriend(IsFriendParam.builder().friendId(friendId).userId(userId).build());
        if(result.getIsSuccessful()){
            localCache.put(key,true);
            redisUtil.set(key,true,1,TimeUnit.HOURS);
            return DeveloperResult.success(serialNo);
        }else{
            // 防止击穿
            localCache.put(key,false);
            redisUtil.set(key,false,10,TimeUnit.MINUTES);
            return DeveloperResult.error(serialNo, result.getMsg());
        }
    }
}
