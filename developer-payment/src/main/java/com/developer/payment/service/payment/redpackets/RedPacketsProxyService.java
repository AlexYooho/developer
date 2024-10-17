package com.developer.payment.service.payment.redpackets;

import com.developer.framework.enums.RedPacketsTypeEnum;
import com.developer.payment.pojo.RedPacketsInfoPO;
import com.developer.payment.repository.RedPacketsInfoRepository;
import com.developer.payment.service.RedPacketsService;
import com.developer.payment.service.register.RedPacketsTypeRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedPacketsProxyService {

    @Autowired
    private RedPacketsInfoRepository redPacketsInfoRepository;

    @Autowired
    private RedPacketsTypeRegister redPacketsTypeRegister;

    public RedPacketsService findInstance(Object id) {
        if(id instanceof Long) {
            Long redPacketsId = (Long) id;
            RedPacketsInfoPO po = redPacketsInfoRepository.getById(redPacketsId);
            if (po != null) {
                return redPacketsTypeRegister.findInstance(po.getType());
            }
        }

        if(id instanceof RedPacketsTypeEnum){
            return redPacketsTypeRegister.findInstance((RedPacketsTypeEnum) id);
        }

        return null;
    }


}
