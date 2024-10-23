package com.developer.payment.service.register;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.RedPacketsService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

public class RedPacketsTypeRegister {

    private final Map<RedPacketsTypeEnum, RedPacketsService> map = new HashMap<>();

    public RedPacketsService findInstance(RedPacketsTypeEnum typeEnum) {
        return map.get(typeEnum);
    }

    public void registerInstance(RedPacketsTypeEnum typeEnum, RedPacketsService redPacketsService) {
        map.put(typeEnum, redPacketsService);
    }

}
