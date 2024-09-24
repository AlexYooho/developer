package com.developer.user.service;

import com.developer.user.dto.ModifyUserInfoDTO;
import com.developer.user.dto.UserInfoDTO;
import com.developer.user.dto.UserRegisterDTO;
import com.developer.framework.model.DeveloperResult;

import java.util.List;

public interface UserService {


    /**
     * 用户注册
     * @param dto
     * @return
     */
    DeveloperResult<Boolean> register(UserRegisterDTO dto);

    /**
     * 获取当前用户信息
     * @return
     */
    DeveloperResult<UserInfoDTO> findSelfUserInfo();

    /**
     * 根据userId查找用户信息
     * @param userId
     * @return
     */
    DeveloperResult<UserInfoDTO> findUserInfoById(Long userId);

    /**
     * 根据用户昵称查找用户
     * @param name
     * @return
     */
    DeveloperResult<List<UserInfoDTO>> findUserByName(String name);

    /**
     * 修改用户信息
     * @param dto
     * @return
     */
    public DeveloperResult<Boolean> modifyUserInfo(ModifyUserInfoDTO dto);

}
