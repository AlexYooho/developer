package com.developer.message.service;

import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;

public interface VerifyCodeService {

    DeveloperResult<Boolean> sendVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount);

    DeveloperResult<Integer> getVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount);

    DeveloperResult<Boolean> checkVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode);

}
