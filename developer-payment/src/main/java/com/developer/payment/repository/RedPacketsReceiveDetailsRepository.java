package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import com.developer.payment.mappers.RedPacketsReceiveDetailsMapper;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedPacketsReceiveDetailsRepository extends ServiceImpl<RedPacketsReceiveDetailsMapper, RedPacketsReceiveDetailsPO> {

    public List<RedPacketsReceiveDetailsPO> findList(Long redPacketsId){
        return this.lambdaQuery().eq(RedPacketsReceiveDetailsPO::getRedPacketsId,redPacketsId).list();
    }

    public RedPacketsReceiveDetailsPO find(Long redPacketsId,Long userId){
        return this.lambdaQuery().eq(RedPacketsReceiveDetailsPO::getRedPacketsId,redPacketsId).eq(RedPacketsReceiveDetailsPO::getReceiveUserId,userId).one();
    }

}
