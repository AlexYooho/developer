package com.developer.payment.service.payment.config;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.payment.LuckRedPacketsService;
import com.developer.payment.service.payment.NormalRedPacketsService;
import com.developer.payment.service.payment.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedPacketsTypeConfig {

    @Autowired
    private NormalRedPacketsService normalRedPacketsService;

    @Autowired
    private LuckRedPacketsService luckRedPacketsService;

    public RedPacketsTypeRegister register(){
        RedPacketsTypeRegister typeRegister = new RedPacketsTypeRegister();
        typeRegister.registerRedPacketsTypeInstance(RedPacketsTypeEnum.NORMAL, normalRedPacketsService);
        typeRegister.registerRedPacketsTypeInstance(RedPacketsTypeEnum.LUCKY, luckRedPacketsService);
        return typeRegister;
    }

}
