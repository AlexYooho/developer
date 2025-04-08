package com.developer.user.service;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.user.dto.SendRegisterVerifyCodeRequestDTO;

public interface VerifyCodeService {

    DeveloperResult<Boolean> sendVerifyCode(SendRegisterVerifyCodeRequestDTO req);

    DeveloperResult<Integer> getVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount);

    DeveloperResult<Boolean> checkVerifyCode(String serialNo,VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode);

}
