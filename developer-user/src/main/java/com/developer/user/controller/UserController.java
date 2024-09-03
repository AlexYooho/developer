package com.developer.user.controller;

import com.developer.user.dto.UserRegisterDTO;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @PostMapping("register")
    public DeveloperResult register(@RequestBody UserRegisterDTO dto){
        return userService.register(dto);
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/modify/password")
    public DeveloperResult modifyPassword(){
        return DeveloperResult.success();
    }

    /**
     * 重置密码
     * @return
     */
    @PutMapping("/reset/password")
    public DeveloperResult resetPassword(){
        return DeveloperResult.success();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("selfInfo")
    public DeveloperResult getSelfUserInfo(){
        return userService.getSelfUserInfo();
    }

}
