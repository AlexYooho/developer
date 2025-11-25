package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.framework.enums.payment.RedPacketsStatusEnum;
import com.developer.payment.mappers.RedPacketsInfoMapper;
import com.developer.payment.pojo.RedPacketsInfoPO;
import org.springframework.stereotype.Repository;

@Repository
public class RedPacketsInfoRepository extends ServiceImpl<RedPacketsInfoMapper, RedPacketsInfoPO> {

    public RedPacketsInfoPO findById(Long id){
        return this.lambdaQuery().eq(RedPacketsInfoPO::getId,id).eq(RedPacketsInfoPO::getStatus, RedPacketsStatusEnum.PENDING).one();
    }

}
