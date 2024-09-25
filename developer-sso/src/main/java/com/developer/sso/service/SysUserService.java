package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.LoginDTO;
import com.developer.sso.dto.TokenDTO;
import com.developer.sso.model.UserInfo;

import java.util.Map;

public interface SysUserService {

    UserInfo getUserByUserName(String userName);

    DeveloperResult<TokenDTO> Login(LoginDTO dto);

}
