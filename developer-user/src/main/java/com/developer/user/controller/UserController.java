package com.developer.user.controller;

import com.developer.user.dto.ModifyUserInfoDTO;
import com.developer.user.dto.UserRegisterDTO;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.UserService;
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
     * @return
     */
    @PutMapping("/modify/password")
    public DeveloperResult modifyPassword(){
        return DeveloperResult.success();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("selfInfo")
    public DeveloperResult getSelfUserInfo(){
        return userService.findSelfUserInfo();
    }

    /**
     * 查找用户根据id
     * @param id
     * @return
     */
    @GetMapping("/find/{id}")
    public DeveloperResult findById(@PathVariable("id") Long id){
        return userService.findUserInfoById(id);
    }

    /**
     * 根据昵称查找用户
     * @param name
     * @return
     */
    @GetMapping("/findByName")
    public DeveloperResult findByName(@RequestParam("name") String name){
        return userService.findUserByName(name);
    }

    /**
     * 修改用户信息
     * @param dto
     * @return
     */
    @PutMapping("/modify")
    public DeveloperResult modifyUserInfo(@RequestBody ModifyUserInfoDTO dto){
        return userService.modifyUserInfo(dto);
    }

}
