package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.TransferInfoMapper;
import com.developer.payment.pojo.TransferInfoPO;
import org.springframework.stereotype.Repository;

@Repository
public class TransferInfoRepository extends ServiceImpl<TransferInfoMapper, TransferInfoPO> {
}
