package com.developer.user.controller;

import com.developer.user.dto.UserRegisterDTO;
import com.developer.user.service.UserService;
import model.DeveloperResult;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @param userId
     * @return
     */
    @PutMapping("{userId}/modify/password")
    public DeveloperResult modifyPassword(@PathVariable Long userId){
        return DeveloperResult.success();
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */
    @PutMapping("{userId}/reset/password")
    public DeveloperResult resetPassword(@PathVariable Long userId){
        return DeveloperResult.success();
    }

    @GetMapping("index")
    public String Index(){
        return "hello world gateway";
    }
}
