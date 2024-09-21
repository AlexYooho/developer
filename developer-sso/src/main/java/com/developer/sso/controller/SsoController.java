package com.developer.sso.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.LoginDTO;
import com.developer.sso.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sso")
public class SsoController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     * @param dto
     * @return
     */
    @PostMapping("login")
    public DeveloperResult login(@RequestBody LoginDTO dto){
        return sysUserService.Login(dto);
    }

}
