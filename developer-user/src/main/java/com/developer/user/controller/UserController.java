package com.developer.user.controller;

import com.developer.user.dto.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public DeveloperResult<Boolean> register(@RequestBody UserRegisterDTO dto){
        return userService.register(dto);
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/modify/password")
    public DeveloperResult<Boolean> modifyPassword(@RequestBody ModifyUserPasswordDTO req){
        return userService.modifyUserPassword(req);
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
     * @param req
     * @return
     */
    @PostMapping("/find")
    public DeveloperResult<UserInfoDTO> findById(@RequestBody FindUserRequestDTO req){
        return userService.findUserInfoById(req);
    }

    /**
     * 根据昵称查找用户
     * @param req
     * @return
     */
    @GetMapping("/findByName")
    public DeveloperResult<List<UserInfoDTO>> findByName(@RequestBody FindUserRequestDTO req){
        return userService.findUserByName(req);
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
     * @param
     * @return
     */
    @GetMapping("/online/terminal")
    public DeveloperResult<List<OnlineTerminalDTO>> onlineTerminal(@RequestParam("user_ids") String userIds){
        FindOnlineTerminalRequestDTO req = new FindOnlineTerminalRequestDTO();
        req.setUserIds(userIds);
        return userService.findOnlineTerminal(req);
    }
}
