package com.developer.user.controller;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.user.client.FriendClient;
import com.developer.user.dto.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.UserService;
import com.developer.user.service.VerifyCodeService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @PostMapping("register")
    public DeveloperResult<Boolean> register(@RequestBody UserRegisterDTO dto){
        return userService.register(dto);
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/modify/password")
    public DeveloperResult<Boolean> modifyPassword(){
        return DeveloperResult.success();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("selfInfo")
    public DeveloperResult<UserInfoDTO> getSelfUserInfo(){
        return userService.findSelfUserInfo();
    }

    /**
     * 查找用户根据id
     * @param id
     * @return
     */
    @GetMapping("/find/{id}")
    public DeveloperResult<UserInfoDTO> findById(@PathVariable("id") Long id){
        return userService.findUserInfoById(id);
    }

    /**
     * 根据昵称查找用户
     * @param name
     * @return
     */
    @GetMapping("/findByName")
    public DeveloperResult<List<UserInfoDTO>> findByName(@RequestParam("name") String name){
        return userService.findUserByName(name);
    }

    /**
     * 修改用户信息
     * @param dto
     * @return
     */
    @PutMapping("/modify")
    public DeveloperResult<Boolean> modifyUserInfo(@RequestBody ModifyUserInfoDTO dto){
        return userService.modifyUserInfo(dto);
    }

    /**
     * 获取在线终端
     * @param userIds
     * @return
     */
    @GetMapping("/online/terminal")
    public DeveloperResult<List<OnlineTerminalDTO>> onlineTerminal(@RequestParam("userIds") String userIds){
        return userService.findOnlineTerminal(userIds);
    }

    /**
     * 发送校验码
     * @param email
     * @return
     */
    @PostMapping("/send/code")
    public DeveloperResult<Boolean> sendRegisterVerifyCode(@RequestParam("email") String email, @RequestParam("verifyCodeType")Integer verifyCodeType){
        return verifyCodeService.sendVerifyCode(VerifyCodeTypeEnum.fromCode(verifyCodeType),email);
    }

}
