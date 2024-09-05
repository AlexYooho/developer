package com.developer.friend.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.friend.mappers.FriendMapper;
import com.developer.friend.pojo.FriendPO;
import org.springframework.stereotype.Repository;

@Repository
public class FriendRepository extends ServiceImpl<FriendMapper, FriendPO> {
}
