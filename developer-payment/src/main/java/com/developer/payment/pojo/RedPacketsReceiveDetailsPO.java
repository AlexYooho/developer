package com.developer.payment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developer.payment.enums.RedPacketsReceiveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ： liaopenghui
 * @date ：Created in 2020/7/27 15:06
 * @description： 红包领取明细
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("red_packets_receive_detail")
public class RedPacketsReceiveDetailsPO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 红包id
     */
    @TableField("red_packets_id")
    private Long redPacketsId;

    /**
     * 领取用户id
     */
    @TableField("receive_user_id")
    private Long receiveUserId;

    /**
     * 领取金额
     */
    @TableField("receive_amount")
    private BigDecimal receiveAmount;

    /**
     * 领取时间
     */
    @TableField("receive_time")
    private Date receiveTime;

    /**
     * 领取状态
     */
    @TableField("status")
    private RedPacketsReceiveStatusEnum status;

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
