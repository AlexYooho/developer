package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.payment.enums.TransactionStatusEnum;
import com.developer.payment.enums.TransactionTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@TableName("wallet_transaction")
public class WalletTransactionPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户钱包id
     */
    @TableField("wallet_id")
    private Long walletId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 交易类型：0-转账 1-充值 2-提现 3 红包
     */
    @TableField("transaction_type")
    private TransactionTypeEnum transactionType;

    /**
     * 交易金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 交易前金额
     */
    @TableField("before_balance")
    private BigDecimal beforeBalance;

    /**
     * 交易后金额
     */
    @TableField("after_balance")
    private BigDecimal afterBalance;

    /**
     * 关联用户id,转账收款人id
     */
    @TableField("related_user_id")
    private Long relatedUserId;

    /**
     * 外部流水编号
     */
    @TableField("reference_id")
    private BigDecimal referenceId;

    /**
     * 交易状态
     */
    @TableField("status")
    private TransactionStatusEnum status;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 修改时间
     */
    @TableField("updated_time")
    private Date updateTime;

}
