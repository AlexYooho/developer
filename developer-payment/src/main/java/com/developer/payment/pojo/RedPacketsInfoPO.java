package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.framework.enums.payment.PaymentChannelEnum;
import com.developer.framework.enums.payment.RedPacketsStatusEnum;
import com.developer.framework.enums.payment.RedPacketsTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 红包表
 * @author liaopenghui
 * @date 2021/3/16 15:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("red_packets_info")
public class RedPacketsInfoPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者id
     */
    @TableField("sender_id")
    private Long senderUserId;

    /**
     * 接收目标id
     */
    @TableField("receive_target_id")
    private Long receiveTargetId;

    /**
     * 红包个数
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 剩余红包个数
     */
    @TableField("remaining_count")
    private Integer remainingCount;

    /**
     * 红包类型
     */
    @TableField("type")
    private RedPacketsTypeEnum type;

    /**
     * 红包状态
     */
    @TableField("status")
    private RedPacketsStatusEnum status;

    /**
     * 关联消息id
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 红包支付渠道
     */
    @TableField("channel")
    private PaymentChannelEnum channel;

    /**
     * 发送金额
     */
    @TableField("send_amount")
    private BigDecimal sendAmount;

    /**
     * 剩余金额
     */
    @TableField("remaining_amount")
    private BigDecimal remainingAmount;

    /**
     * 退回金额
     */
    @TableField("return_amount")
    private BigDecimal returnAmount;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private Date expireTime;

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
