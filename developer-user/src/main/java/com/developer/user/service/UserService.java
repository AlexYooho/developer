package com.developer.user.service;

import com.developer.user.dto.ModifyUserInfoDTO;
import com.developer.user.dto.UserRegisterDTO;
import com.developer.framework.model.DeveloperResult;

public interface UserService {


    /**
     * 用户注册
     * @param dto
     * @return
     */
    DeveloperResult register(UserRegisterDTO dto);

    /**
     * 获取当前用户信息
     * @return
     */
    DeveloperResult findSelfUserInfo();

    /**
     * 根据userId查找用户信息
     * @param userId
     * @return
     */
    DeveloperResult findUserInfoById(Long userId);

    /**
     * 根据用户昵称查找用户
     * @param name
     * @return
     */
    DeveloperResult findUserByName(String name);

    /**
     * 修改用户信息
     * @param dto
     * @return
     */
    public DeveloperResult modifyUserInfo(ModifyUserInfoDTO dto);

}
