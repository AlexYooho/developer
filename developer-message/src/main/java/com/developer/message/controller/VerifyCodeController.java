package com.developer.message.controller;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.CheckVerifyCodeRequestDTO;
import com.developer.message.dto.SendVerifyCodeRequestDTO;
import com.developer.message.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("verify-code")
public class VerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    /**
     * 发送校验码
     * @param req
     * @return
     */
    @PostMapping("/send")
    public DeveloperResult<Boolean> sendRegisterVerifyCode(@RequestBody SendVerifyCodeRequestDTO req){
        return verifyCodeService.sendVerifyCode(req);
    }

    /**
     * 校验验证码
     * @param req
     * @return
     */
    @PostMapping("/check")
    public DeveloperResult<Boolean> checkVerifyCode(@RequestBody CheckVerifyCodeRequestDTO req){
        return verifyCodeService.checkVerifyCode(req);
    }
}
