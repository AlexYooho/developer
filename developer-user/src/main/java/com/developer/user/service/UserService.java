package com.developer.user.service;

import com.developer.user.dto.UserRegisterDTO;
import com.developer.framework.model.DeveloperResult;

public interface UserService {


    /**
     * 用户注册
     * @param dto
     * @return
     */
    DeveloperResult register(UserRegisterDTO dto);

}