package com.developer.user.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.user.mappers.UserMapper;
import com.developer.user.pojo.UserPO;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends ServiceImpl<UserMapper, UserPO> {

    public UserPO findByAccount(String account){
        return this.lambdaQuery().eq(UserPO::getAccount,account).one();
    }

}
