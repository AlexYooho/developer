package com.developer.sso.service;

import com.developer.sso.model.UserInfo;

public interface SysUserService {

    UserInfo getUserByUserName(String userName);

}
