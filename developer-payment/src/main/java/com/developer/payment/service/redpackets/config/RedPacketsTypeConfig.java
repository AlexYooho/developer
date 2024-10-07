package com.developer.payment.service.redpackets.config;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.redpackets.RedPacketsTypeRegister;
import com.developer.payment.service.redpackets.impl.LuckRedPacketsServiceImpl;
import com.developer.payment.service.redpackets.impl.NormalRedPacketServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedPacketsTypeConfig {

    @Autowired
    private NormalRedPacketServiceImpl normalRedPacketsService;

    @Autowired
    private LuckRedPacketsServiceImpl luckRedPacketsService;

    @Bean
    public RedPacketsTypeRegister register(){
        RedPacketsTypeRegister typeRegister = new RedPacketsTypeRegister();
        typeRegister.registerRedPacketsTypeInstance(RedPacketsTypeEnum.NORMAL, normalRedPacketsService);
        typeRegister.registerRedPacketsTypeInstance(RedPacketsTypeEnum.LUCKY, luckRedPacketsService);
        return typeRegister;
    }



}
