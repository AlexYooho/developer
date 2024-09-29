package com.developer.payment.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developer.payment.mappers.WalletTransactionMapper;
import com.developer.payment.pojo.WalletTransactionPO;
import org.springframework.stereotype.Repository;

@Repository
public class WalletTransactionRepository extends ServiceImpl<WalletTransactionMapper, WalletTransactionPO> {
}
