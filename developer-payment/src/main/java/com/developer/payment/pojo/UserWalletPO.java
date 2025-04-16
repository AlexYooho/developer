package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.CurrencyEnum;
import com.developer.payment.enums.WalletStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包表
 */
@Data
@Builder
@TableName("user_wallet")
public class UserWalletPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 支付密码
     */
    @TableField("payment_password")
    private Integer paymentPassword;

    /**
     * 冻结余额
     */
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    /**
     * 累计充值金额
     */
    @TableField("total_recharge")
    private BigDecimal totalRecharge;

    /**
     * 累计提现金额
     */
    @TableField("total_withdraw")
    private BigDecimal totalWithdraw;

    /**
     * 币种
     */
    @TableField("currency")
    private CurrencyEnum currency;

    /**
     * 最近一次交易时间
     */
    @TableField("last_transaction_time")
    private Date lastTransactionTime;

    /**
     * 钱包状态
     */
    @TableField("status")
    private WalletStatusEnum status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;
}
