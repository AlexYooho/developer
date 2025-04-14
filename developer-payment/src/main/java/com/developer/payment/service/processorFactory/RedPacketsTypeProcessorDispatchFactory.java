package com.developer.payment.service.processorFactory;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.impl.redpackets.DefaultRedPacketsTypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedPacketsTypeProcessorDispatchFactory {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DefaultRedPacketsTypeServiceImpl defaultService;

    public RedPacketsService getInstance(RedPacketsTypeEnum redPacketsTypeEnum){
        Map<String, RedPacketsService> beansMap = context.getBeansOfType(RedPacketsService.class);
        RedPacketsService instance = null;
        for (RedPacketsService item : beansMap.values()) {
            if (item.redPacketsType() != redPacketsTypeEnum) {
                continue;
            }

            instance = item;
            break;
        }

        return instance == null ? defaultService : instance;
    }

}
