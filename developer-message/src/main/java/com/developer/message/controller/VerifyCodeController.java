package com.developer.message.controller;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("verify-code")
public class VerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    /**
     * 发送校验码
     * @param emailAddress
     * @return
     */
    @PostMapping("/send")
    public DeveloperResult<Boolean> sendRegisterVerifyCode(@RequestParam("email_address") String emailAddress, @RequestParam("verify_code_type")Integer verifyCodeType){
        return verifyCodeService.sendVerifyCode(VerifyCodeTypeEnum.fromCode(verifyCodeType),emailAddress);
    }
}
