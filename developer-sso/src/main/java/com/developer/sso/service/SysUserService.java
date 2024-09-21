package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.LoginDTO;
import com.developer.sso.model.UserInfo;

public interface SysUserService {

    UserInfo getUserByUserName(String userName);

    DeveloperResult Login(LoginDTO dto);

}
