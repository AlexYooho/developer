package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.LoginDTO;
import com.developer.sso.model.UserInfo;

import java.util.Map;

public interface SysUserService {

    UserInfo getUserByUserName(String userName);

    DeveloperResult<Map<String, Object>> Login(LoginDTO dto);

}
