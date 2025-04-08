package com.developer.message.service.impl;

import com.developer.framework.constant.RedisKeyConstant;
import com.developer.framework.enums.VerifyCodeTypeEnum;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.MailUtil;
import com.developer.framework.utils.RedisUtil;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.message.dto.SendVerifyCodeRequestDTO;
import com.developer.message.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Override
    public DeveloperResult<Boolean> sendVerifyCode(SendVerifyCodeRequestDTO req) {
        String serialNo = !StringUtils.hasText(req.getSerialNo()) ? snowflakeNoUtil.getSerialNo() : req.getSerialNo();
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
        return DeveloperResult.success(snowflakeNoUtil.getSerialNo(),verifyCode);
    }

    @Override
    public DeveloperResult<Boolean> checkVerifyCode(VerifyCodeTypeEnum verifyCodeTypeEnum, String emailAccount, Integer verifyCode) {
        String key = RedisKeyConstant.verifyCode(verifyCodeTypeEnum,emailAccount);
        Integer code = redisUtil.get(key, Integer.class);
        if(code!=null && code.equals(verifyCode)){
            redisUtil.delete(key);
            return DeveloperResult.success(snowflakeNoUtil.getSerialNo());
        }else{
            return DeveloperResult.error("验证码错误");
        }
    }
}
