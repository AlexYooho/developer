package com.developer.sso.service;

import com.developer.sso.model.UserInfo;
import com.developer.sso.pojo.UserPO;
import com.developer.sso.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserInfo getUserByUserName(String userName) {

        UserInfo userInfo = new UserInfo();

        // 查询数据库用户是否存在
        UserPO user = userRepository.findByAccount(userName);
        userInfo.setUserId(user.getId().toString());
        userInfo.setPassword(user.getPassword());
        userInfo.setUsername(userName);

        return userInfo;
    }
}
