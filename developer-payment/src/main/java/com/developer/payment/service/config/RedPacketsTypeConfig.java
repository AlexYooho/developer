package com.developer.payment.service.config;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.payment.redpackets.LuckRedPacketsService;
import com.developer.payment.service.payment.redpackets.NormalRedPacketsService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
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
        typeRegister.registerInstance(RedPacketsTypeEnum.NORMAL, normalRedPacketsService);
        typeRegister.registerInstance(RedPacketsTypeEnum.LUCKY, luckRedPacketsService);
        return typeRegister;
    }

}
