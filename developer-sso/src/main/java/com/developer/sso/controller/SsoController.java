package com.developer.sso.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.*;
import com.developer.sso.service.TokenService;
import com.developer.sso.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sso")
public class SsoController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     * @param dto
     * @return
     */
    @PostMapping("login")
    public DeveloperResult<TokenDTO> login(@RequestBody LoginDTO dto){
        return sysUserService.Login(dto);
    }

    @GetMapping("getAccessToken")
    public  DeveloperResult<TokenDTO> getAccessToken(@RequestBody GetAccessTokenRequestDTO req){
        return  tokenService.getAccessToken(req);
    }

    @GetMapping("refreshAccessToken")
    public  DeveloperResult<TokenDTO> refreshAccessToken(@RequestBody RefreshAccessTokenRequestDTO req){
        return  tokenService.refreshAccessToken(req);
    }

}
