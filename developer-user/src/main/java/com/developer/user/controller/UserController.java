package com.developer.user.controller;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.user.dto.*;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.service.UserService;
import com.developer.user.service.VerifyCodeService;
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

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

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
        return DeveloperResult.success(snowflakeNoUtil.getSerialNo());
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("selfInfo")
    public DeveloperResult<UserInfoDTO> getSelfUserInfo(@RequestParam("serial_no") String serialNo){
        return userService.findSelfUserInfo(serialNo);
    }

    /**
     * 查找用户根据id
     * @param req
     * @return
     */
    @GetMapping("/find")
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
     * @param req
     * @return
     */
    @GetMapping("/online/terminal")
    public DeveloperResult<List<OnlineTerminalDTO>> onlineTerminal(@RequestBody FindOnlineTerminalRequestDTO req){
        return userService.findOnlineTerminal(req);
    }

    /**
     * 发送校验码
     * @param req
     * @return
     */
    @PostMapping("/send/code")
    public DeveloperResult<Boolean> sendRegisterVerifyCode(@RequestBody SendRegisterVerifyCodeRequestDTO req){
        return verifyCodeService.sendVerifyCode(req);
    }

}
