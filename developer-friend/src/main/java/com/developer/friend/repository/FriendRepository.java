package com.developer.friend.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.friend.mappers.FriendMapper;
import com.developer.friend.pojo.FriendPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FriendRepository extends ServiceImpl<FriendMapper, FriendPO> {

    public List<FriendPO> findFriendByUserId(Long userId){
        return this.lambdaQuery().eq(FriendPO::getUserId,userId).list();
    }

    public FriendPO findByFriendId(Long friendId,Long userId){
        return this.lambdaQuery().eq(FriendPO::getUserId,userId)
                .eq(FriendPO::getFriendId,friendId).one();
    }

    public Boolean isFriend(Long userId1,Long userId2){
        return this.lambdaQuery().eq(FriendPO::getUserId,userId1).eq(FriendPO::getFriendId,userId2).count()>0;
    }

}
