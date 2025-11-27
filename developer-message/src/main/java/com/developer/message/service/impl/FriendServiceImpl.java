package com.developer.message.service.impl;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.message.service.FriendService;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImpl implements FriendService {

    @Override
    public DeveloperResult<Boolean> isFriend(Long userId, Long friendId) {

        // 1、查询一级本地缓存,判断是否存在好友关系,查到了就返回结果

        // 2、一级本地缓存没查询到则查询二级redis缓存，判断是否存在好友关系

        // 3、二级redis缓存若存在好友关系，则更新一级本地缓存，并返回结果

        // 4、若一级本地缓存和二级redis缓存都没查询到好友关系，则远程调用好友服务的rpc接口判断是否存在好友关系

        // 5、查询到好友关系后再更新一二级缓存，并返回结果

        return DeveloperResult.success(SerialNoHolder.getSerialNo());
    }
}
