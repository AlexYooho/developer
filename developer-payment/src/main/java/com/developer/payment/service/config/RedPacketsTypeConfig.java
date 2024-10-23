package com.developer.payment.service.config;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.payment.redpackets.LuckRedPacketsService;
import com.developer.payment.service.payment.redpackets.NormalRedPacketsService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RedPacketsTypeConfig {

    @Bean
    public RedPacketsTypeRegister registerRedPacketsTypeInstance(
            NormalRedPacketsService normalRedPacketsService,
            LuckRedPacketsService luckRedPacketsService){
        RedPacketsTypeRegister typeRegister = new RedPacketsTypeRegister();
        typeRegister.registerInstance(RedPacketsTypeEnum.NORMAL, normalRedPacketsService);
        typeRegister.registerInstance(RedPacketsTypeEnum.LUCKY, luckRedPacketsService);
        return typeRegister;
    }

}
