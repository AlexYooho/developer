package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.RedPacketsReceiveDetailsMapper;
import com.developer.payment.pojo.RedPacketsReceiveDetailsPO;
import org.springframework.stereotype.Repository;

@Repository
public class RedPacketsReceiveDetailsRepository extends ServiceImpl<RedPacketsReceiveDetailsMapper, RedPacketsReceiveDetailsPO> {
}
