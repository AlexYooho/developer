package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.WalletTransactionRecordMapper;
import com.developer.payment.pojo.WalletTransactionRecordPO;
import org.springframework.stereotype.Repository;

@Repository
public class WalletTransactionRecordRepository extends ServiceImpl<WalletTransactionRecordMapper, WalletTransactionRecordPO> {

    public WalletTransactionRecordPO findByReferenceId(String referenceId) {
        return this.lambdaQuery().eq(WalletTransactionRecordPO::getReferenceId, referenceId).one();
    }

}
