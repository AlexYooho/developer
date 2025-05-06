package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.PaymentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("send_payment_message_log")
public class SendPaymentMessageLogPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 红包id
     */
    @TableField("transaction_id")
    private Long transactionId;

    /**
     * 操作编号
     */
    @TableField("serial_no")
    private String serialNo;

    /**
     * 发送状态 0-默认 1-请求成功 2-请求失败
     */
    @TableField("send_status")
    private Integer sendStatus;

    /**
     * 支付类型
     */
    @TableField("payment_type")
    private PaymentTypeEnum paymentType;

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

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

}
