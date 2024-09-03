package com.developer.sso.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.sso.mappers.UserMapper;
import com.developer.sso.pojo.UserPO;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends ServiceImpl<UserMapper, UserPO> {

    public UserPO findByAccount(String account){
        return this.lambdaQuery().eq(UserPO::getAccount,account).one();
    }

}
