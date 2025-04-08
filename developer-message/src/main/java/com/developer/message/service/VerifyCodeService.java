package com.developer.message.service;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.message.dto.SendVerifyCodeRequestDTO;

public interface VerifyCodeService {

    DeveloperResult<Boolean> sendVerifyCode(SendVerifyCodeRequestDTO req);

    DeveloperResult<Integer> getVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount);

    DeveloperResult<Boolean> checkVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode);

}
