package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.payment.TransferStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@TableName("transfer_info")
public class TransferInfoPO {
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
     * 接收用户id
     */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /**
     * 转账金额
     */
    @TableField("transfer_amount")
    private BigDecimal transferAmount;

    /**
     * 转账状态
     */
    @TableField("transfer_status")
    private TransferStatusEnum transferStatus;

    /**
     * 转账时间
     */
    @TableField("transfer_time")
    private Date transferTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private Date expireTime;

    /**
     * 退回时间
     */
    @TableField("return_time")
    private Date returnTime;

    /**
     * 退回金额
     */
    @TableField("return_amount")
    private BigDecimal returnAmount;

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
