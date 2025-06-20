package com.developer.user.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.MailUtil;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SerialNoHolder;
import com.developer.user.dto.SendRegisterVerifyCodeRequestDTO;
import com.developer.user.service.VerifyCodeService;
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
    public DeveloperResult<Boolean> sendVerifyCode(SendRegisterVerifyCodeRequestDTO req) {
        String serialNo = SerialNoHolder.getSerialNo();
        if(!mailUtil.verifyEmailAddress(req.getEmailAddress())){
            return DeveloperResult.error(serialNo,500,"请输入正确的邮箱");
        }

        Integer code = mailUtil.sendAuthorizationCode();
        String key = RedisKeyConstant.verifyCode(req.getVerifyCodeType(),req.getEmailAddress());
        redisUtil.set(key,code,5, TimeUnit.MINUTES);

        return DeveloperResult.success(serialNo);
    }

    @Override
    public DeveloperResult<Integer> getVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount) {
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        Integer verifyCode = redisUtil.get(key, Integer.class);
        return DeveloperResult.success(SerialNoHolder.getSerialNo(),verifyCode);
    }

    @Override
    public DeveloperResult<Boolean> checkVerifyCode(String serialNo,VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode) {
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        Integer code = redisUtil.get(key, Integer.class);
        if(code!=null && code.equals(verifyCode)){
            redisUtil.delete(key);
            return DeveloperResult.success(serialNo);
        }else{
            return DeveloperResult.error(serialNo,"验证码错误");
        }
    }
}
