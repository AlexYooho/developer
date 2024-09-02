package com.developer.sso.service;

import com.developer.sso.model.UserInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Override
    public UserInfo getUserByUserName(String userName) {
        ArrayList<String> list = new ArrayList<>();
        list.add("ADMIN");
        UserInfo user = new UserInfo();
        user.setUserId("1111");
        user.setPassword("123321");
        user.setUsername(userName);
        user.setPermissions(list);
        return user;
    }
}
