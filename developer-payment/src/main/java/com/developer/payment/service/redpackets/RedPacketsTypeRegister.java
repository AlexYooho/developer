package com.developer.payment.service.redpackets;

import com.developer.framework.enums.RedPacketsTypeEnum;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedPacketsTypeRegister {


    /**
     * 红包类型集合
     */

    public final Map<RedPacketsTypeEnum, RedPacketsService> map = new HashMap<>();


    /**
     * 获取红包类型实例
     * @param typeEnum
     * @return
     */
    public RedPacketsService findRedPacketsTypeInstance(RedPacketsTypeEnum typeEnum){
        return map.get(typeEnum);
    }


    /**
     * 注册红包类型实例
     * @param typeEnum
     * @param redPacketsService
     */
    public void registerRedPacketsTypeInstance(RedPacketsTypeEnum typeEnum, RedPacketsService redPacketsService){
        this.map.put(typeEnum, redPacketsService);
    }


}
