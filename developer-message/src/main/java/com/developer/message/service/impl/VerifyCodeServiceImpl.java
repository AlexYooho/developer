package com.developer.message.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.MailUtil;
import com.developer.framework.utils.RedisUtil;
import com.developer.message.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public DeveloperResult<Boolean> sendVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount) {
        if(!mailUtil.verifyEmailAddress(emailAccount)){
            return DeveloperResult.error(500,"请输入正确的邮箱");
        }

        Integer code = mailUtil.sendAuthorizationCode();
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        redisUtil.set(key,code,5, TimeUnit.MINUTES);

        return DeveloperResult.success();
    }

    @Override
    public DeveloperResult<Integer> getVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount) {
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        Integer verifyCode = redisUtil.get(key, Integer.class);
        return DeveloperResult.success(verifyCode);
    }

    @Override
    public DeveloperResult<Boolean> checkVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode) {
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        Integer code = redisUtil.get(key, Integer.class);
        if(code!=null && code.equals(verifyCode)){
            redisUtil.delete(key);
            return DeveloperResult.success();
        }else{
            return DeveloperResult.error("验证码错误");
        }
    }
}
