package com.developer.user.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.user.mappers.UserMapper;
import com.developer.user.pojo.UserPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository extends ServiceImpl<UserMapper, UserPO> {

    public UserPO findByAccount(String account){
        return this.lambdaQuery().eq(UserPO::getAccount,account).one();
    }

    public Long findByEmail(String email){
        return this.lambdaQuery().eq(UserPO::getEmail,email).count();
    }

    public List<UserPO> findByName(String name){
        return this.lambdaQuery().like(UserPO::getNickname,name)
                .or()
                .like(UserPO::getUsername,name)
                .last("limit 20").list();
    }

    public List<UserPO> findByUserId(List<Long> userIdList){
        return this.lambdaQuery().in(UserPO::getId,userIdList).list();
    }

}
