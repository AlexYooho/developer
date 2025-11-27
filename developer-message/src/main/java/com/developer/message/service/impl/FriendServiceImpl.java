package com.developer.message.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.service.FriendService;
import com.developer.rpc.client.RpcClient;
import com.developer.rpc.client.RpcExecutor;
import com.developer.rpc.dto.friend.response.FriendInfoResponseRpcDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final RedisUtil redisUtil;
    private final RpcClient rpcClient;

    // 一级本地缓存，过期时间10秒
    private final TimedCache<Long, List<Long>> localCache = CacheUtil.newTimedCache(10000);

    @Override
    public DeveloperResult<Boolean> isFriend(Long userId, Long friendId) {
        // 1、查询一级本地缓存,判断是否存在好友关系,查到了就返回结果
        List<Long> friendIds = localCache.get(userId);
        if (CollUtil.isNotEmpty(friendIds)) {
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), friendIds.contains(friendId));
        }

        // 2、一级本地缓存没查询到则查询二级redis缓存，判断是否存在好友关系
        String redisKey = RedisKeyConstant.FRIENDS_KEY(userId);
        friendIds = redisUtil.get(redisKey, new TypeReference<List<Long>>() {
        });
        if (CollUtil.isNotEmpty(friendIds)) {
            // 更新一级本地缓存
            localCache.put(userId, friendIds);
            return DeveloperResult.success(SerialNoHolder.getSerialNo(), friendIds.contains(friendId));
        }

        // 3、二级redis缓存若存在好友关系，则更新一级本地缓存，并返回结果
        // (Step 2 covers this)

        // 4、若一级本地缓存和二级redis缓存都没查询到好友关系，则远程调用好友服务的rpc接口判断是否存在好友关系
        DeveloperResult<List<FriendInfoResponseRpcDTO>> rpcResult = RpcExecutor
                .execute(() -> rpcClient.friendRpcService.findFriends());
        if (rpcResult.getIsSuccessful() && CollUtil.isNotEmpty(rpcResult.getData())) {
            friendIds = rpcResult.getData().stream()
                    .map(FriendInfoResponseRpcDTO::getId)
                    .collect(Collectors.toList());

            // 5、查询到好友关系后再更新一二级缓存，并返回结果
            localCache.put(userId, friendIds);
            redisUtil.set(redisKey, friendIds, 10, TimeUnit.MINUTES); // Redis缓存10分钟

            return DeveloperResult.success(SerialNoHolder.getSerialNo(), friendIds.contains(friendId));
        }

        // 如果RPC也没查到（比如没有好友），则认为是空列表
        // 缓存空列表防止缓存穿透？
        // 这里简单处理，如果RPC失败或空，则认为不是好友
        return DeveloperResult.success(SerialNoHolder.getSerialNo(), false);
    }
}
