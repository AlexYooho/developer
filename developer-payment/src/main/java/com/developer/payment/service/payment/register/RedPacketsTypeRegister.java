package com.developer.payment.service.payment.register;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.payment.RedPacketsPaymentService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedPacketsTypeRegister {

    private final Map<RedPacketsTypeEnum, RedPacketsPaymentService> map = new HashMap<>();

    public RedPacketsPaymentService findRedPacketsTypeInstance(RedPacketsTypeEnum typeEnum) {
        return map.get(typeEnum);
    }

    public void registerRedPacketsTypeInstance(RedPacketsTypeEnum typeEnum, RedPacketsPaymentService redPacketsPaymentService) {
        map.put(typeEnum, redPacketsPaymentService);
    }

}
