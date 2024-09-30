package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.UserWalletMapper;
import com.developer.payment.pojo.UserWalletPO;
import org.springframework.stereotype.Repository;

@Repository
public class UserWalletRepository extends ServiceImpl<UserWalletMapper, UserWalletPO> {

    public UserWalletPO findByUserId(Long userId) {
        return this.lambdaQuery().eq(UserWalletPO::getUserId, userId).one();
    }

}
